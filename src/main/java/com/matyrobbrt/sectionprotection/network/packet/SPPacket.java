package com.matyrobbrt.sectionprotection.network.packet;

import com.matyrobbrt.sectionprotection.SectionProtection;
import io.github.matyrobbrt.asmutils.wrapper.ConstructorWrapper;
import io.github.matyrobbrt.asmutils.wrapper.SupplierWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;

/**
 * A class implementing this interface <b>must</b> have a constructor which has
 * a {@link FriendlyByteBuf} as the only parameter. <br>
 * Example:
 * <pre>
 * {@code
 *   public class TestPacket implements SPPacket {
 *       // Implemented methods here
 *
 *       public TestPacket(FriendlyByteBuf) {
 *          // Decode
 *       }
 *   }
 * }
 *
 * A static method named {@code getDirection} with a {@link NetworkDirection} as the return type
 * will determine the direction of the packet.
 * </pre>
 */
public interface SPPacket {

    void handle(NetworkEvent.Context context);

    void encode(FriendlyByteBuf buf);

    static <T extends SPPacket> ConstructorWrapper<T> getConstructor(Class<T> clazz) {
        try {
            return ConstructorWrapper.wrap(clazz, FriendlyByteBuf.class);
        } catch (Exception e) {
            SectionProtection.LOGGER.error("Exception while trying to generate constructor invoker for packet class {}: ", clazz, e);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private static <T extends SPPacket> NetworkDirection internal_getPacketDirection(Class<T> clazz) {
        try {
            final var method = clazz.getMethod("getDirection");
            final var wrapper = SupplierWrapper.<NetworkDirection>wrapMethod(method);
            return wrapper.get();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    static <T extends SPPacket> void register(SimpleChannel channel, int index, Class<T> clazz) {
        final var decoder = getConstructor(clazz);
        channel.messageBuilder(clazz, index, internal_getPacketDirection(clazz))
                .decoder(decoder::invoke)
                .encoder(SPPacket::encode)
                .consumer((pkt, sup) -> {
                    final var ctx = sup.get();
                    pkt.handle(ctx);
                    ctx.setPacketHandled(true);
                })
                .add();
    }
}
