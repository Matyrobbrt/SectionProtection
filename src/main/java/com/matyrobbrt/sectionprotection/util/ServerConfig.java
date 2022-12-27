package com.matyrobbrt.sectionprotection.util;

import com.matyrobbrt.sectionprotection.SectionProtection;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.unsafe.UnsafeHacks;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class ServerConfig {

    public static final ForgeConfigSpec SPEC;

    // General
    public static final ForgeConfigSpec.BooleanValue ALWAYS_ALLOW_FAKE_PLAYERS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CONVERSION_ITEMS;
    public static final ForgeConfigSpec.BooleanValue CONSUME_CONVERSION_ITEM;

    // Claiming
    public static final ChunksValueConfig UNCLAIMABLE_CHUNKS;
    public static final ForgeConfigSpec.IntValue CLAIM_RADIUS;
    public static final ForgeConfigSpec.BooleanValue HIGHLIGHT_MAP;
    public static final ForgeConfigSpec.BooleanValue ONLY_FULL_CLAIM;

    // Default Protection
    public static final ChunksValueConfig DEFAULT_MOB_GRIEFING_PROTECTED;
    public static final ChunksValueConfig DEFAULT_EXPLOSION_PROTECTED;

    static {
        final var builder = new WrappingBuilder();

        builder.push("general");
        {
            ALWAYS_ALLOW_FAKE_PLAYERS = builder.comment("If all fake players should be allowed to interact with claimed chunks.",
                    "Note: Enabling this is VERY but VERY risky, as it makes griefing protection useless. Players should whitelist a fake player only if they want to.")
                    .define("always_allow_fake_players", false);

            CONVERSION_ITEMS = builder.comment("A list of items that can be used for converting banners and lecterns into protecting ones.",
                    "Note: While this config exists, items in the tag \"sectionprotection:conversion_item\" will be accepted as well.")
                    .defineList("conversion_items", List.of(Registry.ITEM.getKey(Items.NETHERITE_INGOT).toString()), e -> true);

            CONSUME_CONVERSION_ITEM = builder.comment("If conversion items should be consumed when they convert a banner / lectern.")
                    .define("consume_conversion_item", true);
        }
        builder.pop();

        builder.push("claiming");
        {
            final var defaultUnclaimable = List.of("0,0", "0,1", "1,0", "1,1");
            UNCLAIMABLE_CHUNKS = builder.comment("A list of the coordinates of chunks that shouldn't be claimable.",
                    "The format for a chunk coordinate is: \"x,z\", where x and z are the positions of the chunk. (Can be gotten though the command \"/sectionprotection chunk pos ~ ~ ~\")")
                    .defineChunks("unclaimable_chunks", defaultUnclaimable);

            CLAIM_RADIUS = builder.comment("The radius of the area that a banner claims chunks around.",
                            "0 creates a 1x1 area, 1 creates a 3x3 area, etc.")
                    .defineInRange("claim_radius", 0, 0, Byte.MAX_VALUE);

            HIGHLIGHT_MAP = builder.comment("EXPERIMENTAL FEATURE!", "If the base colour of the banner protecting a chunk should be used to highlight that chunk on a vanilla map.")
                    .define("experimental_highlight_map", false);

            ONLY_FULL_CLAIM = builder.comment("If claiming an area of chunks should be allowed only if none of those chunks are claimed already.")
                    .define("only_full_claim", true);
        }
        builder.pop();

        // TODO document ranged values, like "s10:0,0"
        builder.comment("Protection rules for specific chunks",
                "Most values in this category take in a list of chunk coordinates which have the format: \"x,z\" (x and z being the positions of the chunks, that can be gotten using the command \"/sectionprotection chunk pos ~ ~ ~\")")
            .push("default_protection");
        {
            DEFAULT_MOB_GRIEFING_PROTECTED = builder.comment("A list of the coordinates of chunks that are protected by mob griefing by default.",
                    "Note: Does not prevent breeding villagers")
                    .defineChunks("griefing", List.of());

            DEFAULT_EXPLOSION_PROTECTED = builder.comment("A list of the coordinates of chunks that are protected by explosions by default.")
                    .defineChunks("explosion", List.of());
        }
        builder.pop();

        SPEC = builder.build();
    }

    public static Stream<ChunkPos> getChunksToClaim(ChunkPos centre) {
        return ChunkPos.rangeClosed(centre, CLAIM_RADIUS.get());
    }


    private static Predicate<ForgeConfigSpec.ConfigValue<?>> IS_LOADED;
    static {
        try {
            final var SPEC_FIELD = ForgeConfigSpec.ConfigValue.class.getDeclaredField("spec");
            final var CHILD_CONFIG_FIELD = ForgeConfigSpec.class.getDeclaredField("childConfig");
            IS_LOADED = configValue -> {
                try {
                    return UnsafeHacks.getField(CHILD_CONFIG_FIELD, UnsafeHacks.getField(SPEC_FIELD, configValue)) != null;
                } catch (Exception ex) {
                    return false;
                }
            };
        } catch (Exception exception) {
            IS_LOADED = configValue -> {
                try {
                    configValue.get();
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            };
        }
    }

    @SubscribeEvent
    static void configChanged(ModConfigEvent.Reloading event) {
        SectionProtection.LOGGER.debug("SectionProtection config just got changed on the file system!");
        reloadChunksConfigs(!IS_LOADED.test(ALWAYS_ALLOW_FAKE_PLAYERS));
    }

    @SubscribeEvent
    static void configLoaded(ModConfigEvent.Loading event) {
        SectionProtection.LOGGER.debug("Loaded SectionProtection config file {}", event.getConfig().getFileName());
        reloadChunksConfigs(true);
    }

    private static void reloadChunksConfigs(boolean defaultValue) {
        ChunksValueConfig.ALL.forEach(it -> it.reload(defaultValue));
    }

    public static final class ChunksValueConfig extends Value<Set<ChunkPos>> {
        private static final List<ChunksValueConfig> ALL = new ArrayList<>();

        private final ForgeConfigSpec.ConfigValue<List<? extends String>> cfg;

        private ChunksValueConfig(Set<ChunkPos> defaultValue, ForgeConfigSpec.ConfigValue<List<? extends String>> cfg) {
            super(defaultValue);
            this.cfg = cfg;
            ALL.add(this);
        }

        private void reload(boolean defaultValue) {
            accept((defaultValue ? cfg.getDefault() : cfg.get()).stream().flatMap(s -> Utils.chunkPosFromString(s).stream()).collect(Collectors.toSet()));
        }
    }

    private static final class WrappingBuilder extends ForgeConfigSpec.Builder {
        @Override
        public WrappingBuilder comment(String... comment) {
            return (WrappingBuilder) super.comment(comment);
        }

        @Override
        public WrappingBuilder comment(String comment) {
            return (WrappingBuilder) super.comment(comment);
        }

        public ChunksValueConfig defineChunks(String path, List<String> defaultValues) {
            final var cfg = this.defineListAllowEmpty(List.of(path), () -> defaultValues, e -> true);
            return new ChunksValueConfig(defaultValues.stream().flatMap(str -> Utils.chunkPosFromString(str).stream()).collect(Collectors.toSet()), cfg);
        }
    }
}
