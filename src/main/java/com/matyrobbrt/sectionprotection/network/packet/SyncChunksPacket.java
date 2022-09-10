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

import java.util.Map;

public record SyncChunksPacket(ResourceKey<Level> key, Map<ChunkPos, ChunkData> data) implements SPPacket {
    @Override
    public void handle(NetworkEvent.Context context) {
        this.data.forEach((pos, data) -> MinecraftForge.EVENT_BUS.post(new ChunkDataChangeEvent.Client(
                key, pos, data, null
        )));
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(key.location());
        buf.writeMap(data, FriendlyByteBuf::writeChunkPos, (b, d) -> d.encode(b));
    }

    public static SyncChunksPacket decode(FriendlyByteBuf buf) {
        return new SyncChunksPacket(ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation()), buf.readMap(
                FriendlyByteBuf::readChunkPos, ChunkData::decode
        ));
    }
}
