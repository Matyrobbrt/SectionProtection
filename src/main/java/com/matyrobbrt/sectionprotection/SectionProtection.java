package com.matyrobbrt.sectionprotection;

import com.matyrobbrt.sectionprotection.api.SectionProtectionAPI;
import com.matyrobbrt.sectionprotection.api.event.ChunkDataChangeEvent;
import com.matyrobbrt.sectionprotection.api.event.TeamChangeEvent;
import com.matyrobbrt.sectionprotection.client.SPClient;
import com.matyrobbrt.sectionprotection.commands.SPCommands;
import com.matyrobbrt.sectionprotection.network.SPFeatures;
import com.matyrobbrt.sectionprotection.network.SPNetwork;
import com.matyrobbrt.sectionprotection.network.packet.SyncChunkPacket;
import com.matyrobbrt.sectionprotection.network.packet.SyncChunksPacket;
import com.matyrobbrt.sectionprotection.network.packet.SyncTeamPacket;
import com.matyrobbrt.sectionprotection.recipe.RecipeEnabledCondition;
import com.matyrobbrt.sectionprotection.util.Constants;
import com.matyrobbrt.sectionprotection.util.SPVersion;
import com.matyrobbrt.sectionprotection.util.ServerConfig;
import com.matyrobbrt.sectionprotection.util.Utils;
import com.matyrobbrt.sectionprotection.world.Banners;
import com.matyrobbrt.sectionprotection.world.ClaimedChunks;
import com.mojang.logging.LogUtils;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.jar.Manifest;

// TODO lazy translation if client has mod present
@Mod(SectionProtection.MOD_ID)
public class SectionProtection {

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
        bus.addListener((final FMLCommonSetupEvent event) -> SPNetwork.register());

        MinecraftForge.EVENT_BUS.addListener(SPCommands::register);
        MinecraftForge.EVENT_BUS.register(SectionProtection.class);
        MinecraftForge.EVENT_BUS.register(ProtectionListeners.class);

        if (FMLLoader.getDist() == Dist.CLIENT) {
            new SPClient(bus);
        }
    }

    @SubscribeEvent
    static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            // Send teams first so the info can be there before chunks
            if (SPNetwork.isPresent(player, SPNetwork.TEAM_SYNC_CHANNEL) && SPFeatures.TEAM_SYNC.clientCanReceive(player)) {
                final var ch = SPNetwork.getChannel(SPFeatures.TEAM_SYNC);
                Banners.get(ServerLifecycleHooks.getCurrentServer())
                        .getBanners().forEach((banner, members) -> ch.sendTo(
                                new SyncTeamPacket(banner, members),
                                player.connection.getConnection(),
                                NetworkDirection.PLAY_TO_CLIENT
                        ));
            }

            if (SPNetwork.isPresent(player, SPNetwork.CHUNK_SYNC_CHANNEL) && SPFeatures.CLAIM_SYNC.clientCanReceive(player)) {
                final var ch = SPNetwork.getChannel(SPFeatures.CLAIM_SYNC);
                ServerLifecycleHooks.getCurrentServer().getAllLevels()
                        .forEach(level -> ch.sendTo(new SyncChunksPacket(level.dimension(), ClaimedChunks.get(level).getChunks()),
                                player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT));
            }
        }
    }

    @SubscribeEvent
    static void onChunkChangeData(final ChunkDataChangeEvent.Server event) {
        ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(player -> {
            if (SPNetwork.isPresent(player, SPNetwork.CHUNK_SYNC_CHANNEL)) {
                SPFeatures.CLAIM_SYNC.sendToClient(new SyncChunkPacket(
                        event.dimension, event.pos, event.newData
                ), player);
            }
        });
    }

    @SubscribeEvent
    static void onTeamChange(final TeamChangeEvent.Server event) {
        ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(player -> {
            if (SPNetwork.isPresent(player, SPNetwork.TEAM_SYNC_CHANNEL)) {
                SPFeatures.TEAM_SYNC.sendToClient(new SyncTeamPacket(
                        event.getBanner(), event.getMembers()
                ), player);
            }
        });
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

    public static boolean isConversionItem(ItemStack stack) {
        //noinspection ConstantConditions
        return stack.is(SPTags.IS_CONVERSION_ITEM) || ServerConfig.CONVERSION_ITEMS.get()
            .stream().anyMatch(s -> stack.getItem().getRegistryName().toString().equals(s));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
        //noinspection resource
        var resource = SectionProtection.class.getResourceAsStream("/META-INF/MANIFEST.MF");
        if (resource == null) {
            //noinspection resource
            resource = SectionProtection.class.getResourceAsStream("META-INF/MANIFEST.MF");
        }
        if (resource == null) {
            VERSION = null;
        } else {
            final var finalResource = resource;
            VERSION = Utils.getOrNull(() -> SPVersion.from(new Manifest(finalResource)));
            LamdbaExceptionUtils.uncheck(finalResource::close);
        }
    }
}
