package com.matyrobbrt.sectionprotection.world;

import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.Banner;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ClaimedChunks extends SavedData {

    /**
     * This represents the current version of the SavedData, allowing us to convert data between
     * an older version and a new one.
     */
    public static final int CURRENT_VERSION = 2;

    private final Map<ChunkPos, ChunkData> chunks;

    public ClaimedChunks() {
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

    public void setOwner(ChunkPos pos, Banner newOwner, @Nullable BlockPos bannerPos) {
        chunks.put(pos, new ChunkData(newOwner, bannerPos));
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
        final var version = nbt.getInt("dataVersion");
        final var chunks = new ClaimedChunks();
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
        return level.getDataStorage().computeIfAbsent(ClaimedChunks::load, ClaimedChunks::new,
                SectionProtection.MOD_ID + "_claimed_chunks");
    }

    public record ChunkData(@NotNull Banner banner, @Nullable BlockPos bannerPos) {
        public CompoundTag serialize() {
            final var tag = new CompoundTag();
            tag.put("banner", banner.serialize());
            if (bannerPos != null) {
                tag.putLong("bannerPos", bannerPos.asLong());
            }
            return tag;
        }

        public static ChunkData deserialize(Tag tag) {
            if (tag instanceof ListTag list) {
                return new ChunkData(new Banner(list), null);
            } else if (tag instanceof CompoundTag cTag) {
                return new ChunkData(new Banner(cTag.getList("banner", Tag.TAG_COMPOUND)), BlockPos.of(cTag.getLong("bannerPos")));
            } else
                throw new IllegalArgumentException("Cannot deserialize tag of type " + tag.getType());
        }
    }
}
