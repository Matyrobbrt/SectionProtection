package com.matyrobbrt.sectionprotection.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.function.Function;

public interface SPPacket {

    void handle(NetworkEvent.Context context);

    void encode(FriendlyByteBuf buf);

    static <T extends SPPacket> void register(SimpleChannel channel, int index, Class<T> clazz, Function<FriendlyByteBuf, T> decoder, @Nullable NetworkDirection direction) {
        channel.messageBuilder(clazz, index, direction)
                .decoder(decoder)
                .encoder(SPPacket::encode)
                .consumer((pkt, sup) -> {
                    final var ctx = sup.get();
                    ctx.enqueueWork(() -> pkt.handle(ctx));
                    ctx.setPacketHandled(true);
                })
                .add();
    }

    static <T extends SPPacket> void registerUnsafe(SimpleChannel channel, int index, Class<?> clazz, Function<FriendlyByteBuf, ?> decoder, @Nullable NetworkDirection direction) {
        //noinspection unchecked
        register(channel, index, (Class<T>) clazz, b -> (T) decoder.apply(b), direction);
    }
}
