package com.matyrobbrt.sectionprotection;

import com.matyrobbrt.sectionprotection.api.ClaimedChunk;
import com.matyrobbrt.sectionprotection.api.OneCapProvider;
import com.matyrobbrt.sectionprotection.commands.SPCommands;
import com.matyrobbrt.sectionprotection.util.Constants;
import com.matyrobbrt.sectionprotection.util.SPVersion;
import com.matyrobbrt.sectionprotection.util.Utils;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.jar.Manifest;

// TODO lazy translation if client has mod present
@Mod(SectionProtection.MOD_ID)
public class SectionProtection {

    @Nullable
    public static final SPVersion VERSION;
    public static final String MOD_ID = "sectionprotection";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Item> IS_CONVERSION_ITEM = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MOD_ID, "conversion_item"));

    public SectionProtection() {
        final var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
            () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (ver, remote) -> true));

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, MOD_ID + "-server.toml");

        bus.register(ServerConfig.class);
        bus.addListener(SectionProtection::registerCaps);

        MinecraftForge.EVENT_BUS.addListener(SPCommands::register);
        MinecraftForge.EVENT_BUS.register(SectionProtection.class);
        MinecraftForge.EVENT_BUS.register(ProtectionListeners.class);
    }

    private static final ResourceLocation CAP_ID = new ResourceLocation(MOD_ID, "claimed");

    @SubscribeEvent
    static void attachChunkCaps(final AttachCapabilitiesEvent<LevelChunk> event) {
        event.addCapability(CAP_ID, new OneCapProvider<>(ClaimedChunk.CAPABILITY, ClaimedChunk.Impl::new));
    }

    @SubscribeEvent
    static void onItemToss(final ItemTossEvent event) {
        final var item = event.getEntityItem().getItem();
        if (item.getItem() == Items.WRITTEN_BOOK && item.getOrCreateTag().contains(Constants.SP_GUIDE_TAG)) {
            event.getEntityItem().kill();
        }
    }

    static void registerCaps(final RegisterCapabilitiesEvent event) {
        event.register(ClaimedChunk.class);
    }

    @SuppressWarnings("all")
    public static boolean isConversionItem(ItemStack stack) {
        return stack.is(IS_CONVERSION_ITEM) || ServerConfig.CONVERSION_ITEMS.get()
            .stream().anyMatch(s -> stack.getItem().getRegistryName().toString().equals(s));
    }

    public static boolean canClaimChunk(@Nullable Player player, LevelChunk chunk) {
        final var canClaim = ServerConfig.UNCLAIMABLE_CHUNKS.get().contains(chunk.getPos());
        if (!canClaim && player != null)
            player.displayClientMessage(new TextComponent("This chunk cannot be claimed!")
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
