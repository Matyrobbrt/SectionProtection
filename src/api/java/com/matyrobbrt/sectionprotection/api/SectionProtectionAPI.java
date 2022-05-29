package com.matyrobbrt.sectionprotection.api;

import com.matyrobbrt.sectionprotection.api.chunk.ChunkManager;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;

import java.util.ServiceLoader;

public interface SectionProtectionAPI {
    String MOD_ID = "sectionprotection";

    SectionProtectionAPI INSTANCE = Util.make(() -> {
        final var loader = ServiceLoader.load(SectionProtectionAPI.class).iterator();
        if (!loader.hasNext()) {
            throw new NullPointerException("No SectionProtectionAPI was found on the classpath");
        }
        final var api = loader.next();
        if (loader.hasNext()) {
            throw new IllegalArgumentException("More than one SectionProtectionAPI was found!");
        }
        return api;
    });

    ChunkManager getManager(ServerLevel level);

    <T> void registerPredicate(ActionType<T> actionType, T predicate);
}
