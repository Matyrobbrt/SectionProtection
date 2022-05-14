package com.matyrobbrt.sectionprotection.network;

import java.util.Optional;
import java.util.function.Function;

import com.matyrobbrt.sectionprotection.SectionProtection;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.simple.SimpleChannel;

public final class SPNetwork {

    //@formatter:off
    // TODO this needs versioning
    public static final SimpleChannel CHANNEL = ChannelBuilder.named(new ResourceLocation(SectionProtection.MOD_ID, "channel"))
        .clientAcceptedVersions(v -> true)
        .serverAcceptedVersions(v -> true)
        .networkProtocolVersion(() -> ":)")
        .simpleChannel();
    
    private static int id;
    
    public static void register() {
        registerCS(RequestTeamsPacket.class, RequestTeamsPacket::decode);
        registerSC(RequestTeamsPacket.Response.class, RequestTeamsPacket.Response::decode);
    }
    
    private static <P extends SPPacket> void registerCS(Class<P> clazz, Function<FriendlyByteBuf, P> decoder) {
        CHANNEL.registerMessage(id++, clazz, SPPacket::encode, decoder, (m, c) -> SPPacket.handlePacket(m, c.get()), Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
    
    private static <P extends SPPacket> void registerSC(Class<P> clazz, Function<FriendlyByteBuf, P> decoder) {
        CHANNEL.registerMessage(id++, clazz, SPPacket::encode, decoder, (m, c) -> SPPacket.handlePacket(m, c.get()), Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    
}
