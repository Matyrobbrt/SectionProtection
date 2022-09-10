package com.matyrobbrt.sectionprotection.api.client;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ServiceLoader;

/**
 * The interface used for interacting with SectionProtection's <strong>client-only</strong> API.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface SectionProtectionClientAPI {

    /**
     * The singleton SectionProtection Client API instance.
     */
    SectionProtectionClientAPI INSTANCE = Util.make(() -> {
        final var loader = ServiceLoader.load(SectionProtectionClientAPI.class).iterator();
        if (!loader.hasNext()) {
            throw new NullPointerException("No SectionProtectionClientAPI was found on the classpath");
        }
        final var api = loader.next();
        if (loader.hasNext()) {
            throw new IllegalArgumentException("More than one SectionProtectionClientAPI was found!");
        }
        return api;
    });

    /**
     * Call this method during mod constructor to enable the synchronization of claim status.
     *
     * @apiNote multiple mods may call this method without issues
     */
    void enableClaimSync();

    /**
     * Call this method during mod constructor to enable the synchronization of teams.
     *
     * @apiNote multiple mods may call this method without issues
     */
    void enableTeamSync();
}