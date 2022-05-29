package com.matyrobbrt.sectionprotection.api.chunk;

import com.matyrobbrt.sectionprotection.api.Banner;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

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

    public void encode(FriendlyByteBuf buf) {
        banner.encode(buf);
        buf.writeBoolean(bannerPos != null);
        if (bannerPos != null)
            buf.writeBlockPos(bannerPos);
    }

    public static ChunkData decode(FriendlyByteBuf buf) {
        final var banner = Banner.decode(buf);
        final boolean hasPos = buf.readBoolean();
        return new ChunkData(banner, hasPos ? buf.readBlockPos() : null);
    }
}
