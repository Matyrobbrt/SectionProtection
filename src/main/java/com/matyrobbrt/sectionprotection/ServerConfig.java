package com.matyrobbrt.sectionprotection;

import net.minecraft.core.Registry;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

@SuppressWarnings("deprecation")
public class ServerConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ALLOW_FAKE_PLAYERS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CONVERSION_ITEMS;

    static {
        final var builder = new ForgeConfigSpec.Builder();

        builder.push("general");
        {
            ALLOW_FAKE_PLAYERS = builder.comment("If fake players should be allowed to interact with claimed chunks.")
                    .define("allow_fake_players", true);

            CONVERSION_ITEMS = builder.comment("A list of items that can be used for converting banners and lecterns into protecting ones.",
                    "Note: While this config exists, items in the tag \"sectionprotection:conversion_item\" will be accepted as well.")
                    .defineList("conversion_items", List.of(Registry.ITEM.getKey(Items.NETHERITE_INGOT).toString()), e -> true);
        }
        builder.pop();

        SPEC = builder.build();
    }
}
