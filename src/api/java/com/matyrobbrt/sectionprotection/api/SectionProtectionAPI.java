package com.matyrobbrt.sectionprotection.api;

import com.matyrobbrt.sectionprotection.api.banner.BannerManager;
import com.matyrobbrt.sectionprotection.api.chunk.ChunkManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ServiceLoader;

/**
 * The interface used for interacting with SectionProtection's API.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface SectionProtectionAPI {
    /**
     * The SectionProtection mod id.
     */
    String MOD_ID = "sectionprotection";

    /**
     * The singleton SectionProtection API instance.
     */
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

    /**
     * Gets the {@link ChunkManager} of a level.
     * @param level the level
     * @return the level's chunk manager
     */
    ChunkManager getChunkManager(ServerLevel level);

    /**
     * Gets the {@link BannerManager} of a server.
     * @param server the server
     * @return the server's banner manager
     */
    BannerManager getBannerManager(MinecraftServer server);

    /**
     * Registers an action predicate.
     * @param actionType the type of the action
     * @param predicate the predicate to register
     * @param <T> the type of the predicate
     */
    <T> void registerPredicate(ActionType<T> actionType, T predicate);

}
