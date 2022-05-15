package com.matyrobbrt.sectionprotection.util.codec;

import java.util.List;

import com.mojang.serialization.Codec;

public interface Codecs {

    static <T extends Enum<T>> EnumSetCodec<T> enumSet(Codec<T> codec) {
        return new EnumSetCodec<>(codec);
    }

    static <T extends Enum<T>> Codec<T> forEnum(Class<T> clazz) {
        return Codec.INT.xmap(i -> clazz.getEnumConstants()[i], Enum::ordinal);
    }

    static <T> Codec<List<T>> mutableList(Codec<T> codec) {
        return new MutableListCodec<>(codec);
    }

}
