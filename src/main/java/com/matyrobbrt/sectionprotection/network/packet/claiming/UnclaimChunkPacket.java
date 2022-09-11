package com.matyrobbrt.sectionprotection.network.packet.claiming;

import com.matyrobbrt.sectionprotection.api.extensions.BannerExtension;
import com.matyrobbrt.sectionprotection.network.packet.SPPacket;
import com.matyrobbrt.sectionprotection.world.Banners;
import com.matyrobbrt.sectionprotection.world.ClaimedChunks;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public record UnclaimChunkPacket(ResourceKey<Level> dimension, ChunkPos pos, boolean removeBanner) implements SPPacket {

    @Override
    @SuppressWarnings("ConstantConditions")
    public void handle(NetworkEvent.Context context) {
        if (context.getSender() == null) return;
        final var manager = ClaimedChunks.get(context.getSender().level);
        if (!manager.isOwned(pos)) return;
        final var owner = manager.getOwner(pos);
        if (!Banners.get(context.getSender().server).isMember(owner.banner(), context.getSender().getUUID())) return;
        manager.removeData(pos);
        if (removeBanner && owner.bannerPos() != null) {
            final var level = context.getSender().getServer().getLevel(dimension);
            if (level != null && level.getBlockEntity(owner.bannerPos()) instanceof BannerExtension ext) {
                ext.setSectionProtectionIsUnloaded(false);
                ext.sectionProtectionUnclaim();
                final var state = level.getBlockState(owner.bannerPos());
                Block.getDrops(state, level, owner.bannerPos(), (BlockEntity) ext).forEach((stack) -> Block.popResource(level, context.getSender().blockPosition(), stack));
                state.spawnAfterBreak(level, context.getSender().blockPosition(), ItemStack.EMPTY, false);
                level.destroyBlock(owner.bannerPos(), false, context.getSender());
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension.location());
        buf.writeChunkPos(pos);
        buf.writeBoolean(removeBanner);
    }

    public static UnclaimChunkPacket decode(FriendlyByteBuf buf) {
        return new UnclaimChunkPacket(ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation()), buf.readChunkPos(), buf.readBoolean());
    }
}
