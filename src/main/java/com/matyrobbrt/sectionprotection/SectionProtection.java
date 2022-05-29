package com.matyrobbrt.sectionprotection;

import com.matyrobbrt.sectionprotection.api.ActionType;
import com.matyrobbrt.sectionprotection.api.SectionProtectionAPI;
import com.matyrobbrt.sectionprotection.client.SPClient;
import com.matyrobbrt.sectionprotection.commands.SPCommands;
import com.matyrobbrt.sectionprotection.recipe.RecipeEnabledCondition;
import com.matyrobbrt.sectionprotection.util.Constants;
import com.matyrobbrt.sectionprotection.util.SPVersion;
import com.matyrobbrt.sectionprotection.util.ServerConfig;
import com.matyrobbrt.sectionprotection.util.Utils;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.jar.Manifest;

// TODO lazy translation if client has mod present
@Mod(SectionProtection.MOD_ID)
public class SectionProtection {

    static {
        // io.github.matyrobbrt.asmutils.ClassNameGenerator.setBasePackageName(SectionProtection.class.getPackageName() + ".asm");
    }

    @Nullable
    public static final SPVersion VERSION;
    public static final String MOD_ID = SectionProtectionAPI.MOD_ID;
    public static final Logger LOGGER = LogUtils.getLogger();

    public SectionProtection() {
        final var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
            () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (ver, remote) -> true));

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, MOD_ID + "-server.toml");

        bus.register(ServerConfig.class);
        bus.addGenericListener(RecipeSerializer.class, (final RegistryEvent.Register<RecipeSerializer<?>> event) -> CraftingHelper.register(new RecipeEnabledCondition.Serializer()));
        // bus.addListener((final FMLCommonSetupEvent event) -> SPNetwork.register());

        MinecraftForge.EVENT_BUS.addListener(SPCommands::register);
        MinecraftForge.EVENT_BUS.register(SectionProtection.class);
        MinecraftForge.EVENT_BUS.register(ProtectionListeners.class);

        if (FMLLoader.getDist() == Dist.CLIENT) {
            new SPClient(bus);
        }

        SectionProtectionAPI.INSTANCE.registerPredicate(ActionType.PLACING, ((player, blockSnapshot, placedAgainst) -> ActionType.Result.ALLOW));
        SectionProtectionAPI.INSTANCE.registerPredicate(ActionType.BREAKING, (player, world, pos, state) -> state.getBlock() == Blocks.STONE ? ActionType.Result.DENY : ActionType.Result.ALLOW);
    }

    @SubscribeEvent
    static void onItemToss(final ItemTossEvent event) {
        final var item = event.getEntityItem().getItem();
        if (item.getItem() == Items.WRITTEN_BOOK && item.getOrCreateTag().contains(Constants.SP_GUIDE_TAG)) {
            event.getEntityItem().kill();
        }
    }

    @SubscribeEvent
    static void onServerStart(final ServerStartedEvent event) {
        if (ServerConfig.ALWAYS_ALLOW_FAKE_PLAYERS.get()) {
            LOGGER.warn("All FakePlayers have been granted full permission in claimed chunks! This is a very dangerous config option to enable, use at your own risk.");
        }
    }

    @SuppressWarnings("all")
    public static boolean isConversionItem(ItemStack stack) {
        return stack.is(SPTags.IS_CONVERSION_ITEM) || ServerConfig.CONVERSION_ITEMS.get()
            .stream().anyMatch(s -> stack.getItem().getRegistryName().toString().equals(s));
    }

    public static boolean canClaimChunk(@Nullable Player player, ChunkPos chunk) {
        final var canClaim = !ServerConfig.UNCLAIMABLE_CHUNKS.get().contains(chunk);
        if (!canClaim && player != null)
            player.displayClientMessage(new TextComponent("The chunk at ")
                            .append(new TextComponent(chunk.getMiddleBlockPosition(64).toShortString()).withStyle(ChatFormatting.BLUE))
                            .append(" cannot be claimed!")
                    .withStyle(ChatFormatting.RED), true);
        return canClaim;
    }

    static {
        var resource = SectionProtection.class.getResourceAsStream("/META-INF/MANIFEST.MF");
        if (resource == null) {
            resource = SectionProtection.class.getResourceAsStream("META-INF/MANIFEST.MF");
        }
        if (resource == null) {
            VERSION = null;
        } else {
            final var finalResource = resource;
            VERSION = Utils.getOrNull(() -> SPVersion.from(new Manifest(finalResource)));
        }
    }
}
