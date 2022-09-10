package com.matyrobbrt.sectionprotection.network.packet;

import com.matyrobbrt.sectionprotection.api.chunk.ChunkData;
import com.matyrobbrt.sectionprotection.api.event.ChunkDataChangeEvent;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;

public record SyncChunkPacket(ResourceKey<Level> key, ChunkPos pos, @Nullable ChunkData data) implements SPPacket {
    @Override
    public void handle(NetworkEvent.Context context) {
        MinecraftForge.EVENT_BUS.post(new ChunkDataChangeEvent.Client(
                key, pos, data, null
        ));
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(key.location());
        buf.writeChunkPos(pos);
        if (data == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            data.encode(buf);
        }
    }

    public static SyncChunkPacket decode(FriendlyByteBuf buf) {
        return new SyncChunkPacket(ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation()), buf.readChunkPos(), buf.readBoolean() ? ChunkData.decode(buf) : null);
    }
}
