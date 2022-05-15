package com.matyrobbrt.sectionprotection;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ALLOW_FAKE_PLAYERS;

    static {
        final var builder = new ForgeConfigSpec.Builder();

        builder.push("general");
        {
            ALLOW_FAKE_PLAYERS = builder.comment("If fake players should be allowed to interact with claimed chunks.")
                    .define("allow_fake_players", true);
        }
        builder.pop();

        SPEC = builder.build();
    }
}
