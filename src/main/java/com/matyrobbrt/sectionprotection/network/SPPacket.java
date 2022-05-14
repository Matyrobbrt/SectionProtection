package com.matyrobbrt.sectionprotection.network;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

public interface SPPacket {

    void encode(FriendlyByteBuf buffer);

    void handle(NetworkEvent.Context context);

    static void handlePacket(SPPacket pkt, NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> pkt.handle(ctx));
        ctx.setPacketHandled(true);
    }
}
