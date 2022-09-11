package com.matyrobbrt.sectionprotection.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLE_JOURNEYMAP_INTEGRATION;

    static {
        final var builder = new ForgeConfigSpec.Builder();

        {
            builder.comment("Configs for integration with different mods.")
                    .push("integration");

            {
                builder.comment("JourneyMap integration configuration.")
                        .push("journeymap");

                ENABLE_JOURNEYMAP_INTEGRATION = builder.comment("If JourneyMap integration should be enabled.")
                                .define("enabled", true);

                builder.pop();
            }

            builder.pop();
        }

        SPEC = builder.build();
    }
}
