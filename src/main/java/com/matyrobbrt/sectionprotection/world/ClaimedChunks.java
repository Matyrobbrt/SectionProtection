package com.matyrobbrt.sectionprotection.world;

import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.chunk.ChunkData;
import com.matyrobbrt.sectionprotection.api.chunk.ChunkManager;
import com.matyrobbrt.sectionprotection.api.event.ChunkDataChangeEvent;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ClaimedChunks extends SavedData implements ChunkManager {

    /**
     * This represents the current version of the SavedData, allowing us to convert data between
     * an older version and a new one.
     */
    public static final int CURRENT_VERSION = 2;

    private final Map<ChunkPos, ChunkData> chunks;
    private final ResourceKey<Level> dimension;

    private ClaimedChunks(ResourceKey<Level> dimension) {
        this.dimension = dimension;
        this.chunks = new HashMap<>();
    }

    @Nullable
    public ChunkData getOwner(ChunkPos pos) {
        return chunks.get(pos);
    }

    @Nullable
    public ChunkData getOwner(BlockPos pos) {
        return getOwner(new ChunkPos(pos));
    }

    public boolean isOwned(ChunkPos pos) {
        return chunks.get(pos) != null;
    }

    public boolean isOwned(BlockPos pos) {
        return isOwned(new ChunkPos(pos));
    }

    @Override
    public void setData(ChunkPos pos, ChunkData data) {
        final var old = chunks.put(pos, data);
        fireEvent(pos, data, old);
        setDirty();
    }

    @Override
    public ChunkData removeData(ChunkPos pos) {
        final var data = chunks.remove(pos);
        setDirty();
        fireEvent(pos, null, data);
        return data;
    }

    public void removeOwner(BlockPos pos) {
        removeData(new ChunkPos(pos));
    }

    public Map<ChunkPos, ChunkData> getChunks() {
        return Collections.unmodifiableMap(chunks);
    }

    private void fireEvent(ChunkPos pos, @Nullable ChunkData newData, @Nullable ChunkData oldData) {
        MinecraftForge.EVENT_BUS.post(new ChunkDataChangeEvent.Server(dimension, pos, newData, oldData));
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag pCompoundTag) {
        final var datas = new ListTag();
        chunks.forEach((pos, banner) -> {
            final var data = new CompoundTag();
            data.put("owner", banner.serialize());
            data.putLong("pos", pos.toLong());
            datas.add(data);
        });
        pCompoundTag.put("data", datas);
        pCompoundTag.putInt("dataVersion", CURRENT_VERSION);
        return pCompoundTag;
    }

    public static ClaimedChunks load(ResourceKey<Level> dimension, CompoundTag nbt) {
        //noinspection unused
        final var version = nbt.getInt("dataVersion");
        final var chunks = new ClaimedChunks(dimension);
        nbt.getList("data", Tag.TAG_COMPOUND).forEach(tag -> {
            final var cTag = (CompoundTag) tag;
            chunks.chunks.put(new ChunkPos(cTag.getLong("pos")), ChunkData.deserialize(cTag.get("owner")));
        });
        return chunks;
    }

    public static ClaimedChunks get(Level level) {
        return get((ServerLevel) level);
    }

    public static ClaimedChunks get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(compoundTag -> load(level.dimension(), compoundTag), () -> new ClaimedChunks(level.dimension()),
                SectionProtection.MOD_ID + "_claimed_chunks");
    }

}
