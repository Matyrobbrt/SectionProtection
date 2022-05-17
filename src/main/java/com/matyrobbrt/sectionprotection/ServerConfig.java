package com.matyrobbrt.sectionprotection;

import com.matyrobbrt.sectionprotection.util.Utils;
import com.matyrobbrt.sectionprotection.util.Value;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class ServerConfig {

    public static final ForgeConfigSpec SPEC;

    // General
    public static final ForgeConfigSpec.BooleanValue ALLOW_FAKE_PLAYERS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CONVERSION_ITEMS;

    // Claiming
    public static final ChunksValueConfig UNCLAIMABLE_CHUNKS;

    // Default Protection
    public static final ChunksValueConfig DEFAULT_MOB_GRIEFING_PROTECTED;
    public static final ChunksValueConfig DEFAULT_EXPLOSION_PROTECTED;

    static {
        final var builder = new WrappingBuilder();

        builder.push("general");
        {
            ALLOW_FAKE_PLAYERS = builder.comment("If fake players should be allowed to interact with claimed chunks.")
                    .define("allow_fake_players", true);

            CONVERSION_ITEMS = builder.comment("A list of items that can be used for converting banners and lecterns into protecting ones.",
                    "Note: While this config exists, items in the tag \"sectionprotection:conversion_item\" will be accepted as well.")
                    .defineList("conversion_items", List.of(Registry.ITEM.getKey(Items.NETHERITE_INGOT).toString()), e -> true);
        }
        builder.pop();

        builder.push("claiming");
        {
            final var defaultUnclaimable = List.of("0,0", "0,1", "1,0", "1,1");
            UNCLAIMABLE_CHUNKS = builder.comment("A list of the coordinates of chunks that shouldn't be claimable.",
                    "The format for a chunk coordinate is: \"x,z\", where x and z are the positions of the chunk. (Can be gotten though the command \"/sectionprotection chunk pos ~ ~ ~\")")
                    .defineChunks("unclaimable_chunks", defaultUnclaimable);
        }
        builder.pop();

        builder.comment("Protection rules for specific chunks")
            .comment("Most values in this category take in a list of chunk coordinates which have the format: \"x,z\" (x and z being the positions of the chunks, that can be gotten using the command \"/sectionprotection chunk pos ~ ~ ~\")")
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

    @SubscribeEvent
    static void configChanged(ModConfigEvent.Reloading event) {
        SectionProtection.LOGGER.debug("Loaded SectionProtection config file {}", event.getConfig().getFileName());
        reloadChunksConfigs();
    }

    @SubscribeEvent
    static void configLoaded(ModConfigEvent.Loading event) {
        SectionProtection.LOGGER.debug("SectionProtection config just got changed on the file system!");
        reloadChunksConfigs();
    }

    private static void reloadChunksConfigs() {
        ChunksValueConfig.ALL.forEach(ChunksValueConfig::reload);
    }

    public static final class ChunksValueConfig extends Value<List<ChunkPos>> {
        private static final List<ChunksValueConfig> ALL = new ArrayList<>();

        private final ForgeConfigSpec.ConfigValue<List<? extends String>> cfg;

        private ChunksValueConfig(List<ChunkPos> defaultValue, ForgeConfigSpec.ConfigValue<List<? extends String>> cfg) {
            super(defaultValue);
            this.cfg = cfg;
            ALL.add(this);
        }

        private void reload() {
            accept(cfg.get().stream().map(Utils::chunkPosFromString).toList());
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
            final var cfg = defineList(path, defaultValues, e -> e.toString().split("-").length == 2);
            return new ChunksValueConfig(defaultValues.stream().map(Utils::chunkPosFromString).toList(), cfg);
        }
    }
}
