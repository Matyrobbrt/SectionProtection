package com.matyrobbrt.sectionprotection.world;

import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.Banner;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClaimedChunks extends SavedData {

    /**
     * This represents the current version of the SavedData, allowing us to convert data between
     * an older version and a new one.
     */
    public static final int CURRENT_VERSION = 1;

    private final Map<ChunkPos, Banner> chunks;

    public ClaimedChunks() {
        this.chunks = new HashMap<>();
    }

    @Nullable
    public Banner getOwner(ChunkPos pos) {
        return chunks.get(pos);
    }

    @Nullable
    public Banner getOwner(BlockPos pos) {
        return getOwner(new ChunkPos(pos));
    }

    public boolean isOwned(ChunkPos pos) {
        return chunks.get(pos) != null;
    }

    public boolean isOwned(BlockPos pos) {
        return isOwned(new ChunkPos(pos));
    }

    public void setOwner(ChunkPos pos, Banner newOwner) {
        chunks.put(pos, newOwner);
        setDirty();
    }

    public void clearOwner(ChunkPos pos) {
        chunks.remove(pos);
        setDirty();
    }

    public void clearOwner(BlockPos pos) {
        clearOwner(new ChunkPos(pos));
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

    public static ClaimedChunks load(CompoundTag nbt) {
        final var chunks = new ClaimedChunks();
        nbt.getList("data", Tag.TAG_COMPOUND).forEach(tag -> {
            final var cTag = (CompoundTag) tag;
            chunks.chunks.put(new ChunkPos(cTag.getLong("pos")), new Banner(cTag.getList("owner", Tag.TAG_COMPOUND)));
        });
        return chunks;
    }

    public static ClaimedChunks get(@Nonnull Level level) {
        return get(Objects.requireNonNull(level.getServer()));
    }

    public static ClaimedChunks get(MinecraftServer server) {
        return Objects.requireNonNull(server.getLevel(Level.OVERWORLD)).getDataStorage().computeIfAbsent(ClaimedChunks::load, ClaimedChunks::new,
                SectionProtection.MOD_ID + "_claimed_chunks");
    }

}
