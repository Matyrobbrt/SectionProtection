package com.matyrobbrt.sectionprotection;

import com.matyrobbrt.sectionprotection.api.ClaimedChunk;
import com.matyrobbrt.sectionprotection.api.OneCapProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;

@Mod(SectionProtection.MOD_ID)
public class SectionProtection {

    public static final String MOD_ID = "sectionprotection";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Item> IS_CONVERSION_ITEM = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MOD_ID, "conversion_item"));

    public SectionProtection() {
        final var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
            () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (ver, remote) -> true));

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, MOD_ID + "-server.toml");

        bus.addListener(SectionProtection::registerCaps);

        MinecraftForge.EVENT_BUS.register(SectionProtection.class);
    }

    private static final ResourceLocation CAP_ID = new ResourceLocation(MOD_ID, "claimed");

    @SubscribeEvent
    static void attachChunkCaps(final AttachCapabilitiesEvent<LevelChunk> event) {
        event.addCapability(CAP_ID, new OneCapProvider<>(ClaimedChunk.CAPABILITY, ClaimedChunk.Impl::new));
    }

    static void registerCaps(final RegisterCapabilitiesEvent event) {
        event.register(ClaimedChunk.class);
    }
}
