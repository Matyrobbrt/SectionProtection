package com.matyrobbrt.sectionprotection.network;

import java.util.Collection;

import com.matyrobbrt.sectionprotection.util.ListanableObject;
import com.matyrobbrt.sectionprotection.world.TeamRegistry;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor;

public class RequestTeamsPacket implements SPPacket {

    public static final RequestTeamsPacket INSTANCE = new RequestTeamsPacket();

    public static final ListanableObject<Collection<String>> TEAMS = new ListanableObject<>();

    @Override
    public void encode(FriendlyByteBuf buffer) {
    }

    @Override
    public void handle(Context context) {
        final var isOp = context.getSender().getServer().getPlayerList().isOp(context.getSender().getGameProfile());
        final var reg = TeamRegistry.get(context.getSender().server);
        final var data = isOp ? reg.getAllTeams().keySet() : reg.getTeams(context.getSender().getUUID());
        SPNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(context::getSender), new Response(data));
    }

    static RequestTeamsPacket decode(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    private RequestTeamsPacket() {
    }

    public record Response(Collection<String> response) implements SPPacket {

        @Override
        public void encode(FriendlyByteBuf buffer) {
            buffer.writeCollection(response, FriendlyByteBuf::writeUtf);
        }

        @Override
        public void handle(Context context) {
            RequestTeamsPacket.TEAMS.set(response);
        }

        static Response decode(FriendlyByteBuf buf) {
            return new Response(buf.readList(FriendlyByteBuf::readUtf));
        }
    }
}
