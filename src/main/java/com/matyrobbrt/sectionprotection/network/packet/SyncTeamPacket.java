package com.matyrobbrt.sectionprotection.network.packet;

import com.matyrobbrt.sectionprotection.api.banner.Banner;
import com.matyrobbrt.sectionprotection.api.chunk.ChunkData;
import com.matyrobbrt.sectionprotection.api.event.ChunkDataChangeEvent;
import com.matyrobbrt.sectionprotection.api.event.TeamChangeEvent;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public record SyncTeamPacket(Banner banner, List<UUID> members) implements SPPacket {
    @Override
    public void handle(NetworkEvent.Context context) {
        MinecraftForge.EVENT_BUS.post(new TeamChangeEvent.Client(
                banner, members
        ));
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        banner.encode(buf);
        buf.writeCollection(members, FriendlyByteBuf::writeUUID);
    }

    public static SyncTeamPacket decode(FriendlyByteBuf buf) {
        return new SyncTeamPacket(Banner.decode(buf), buf.readList(FriendlyByteBuf::readUUID));
    }
}
