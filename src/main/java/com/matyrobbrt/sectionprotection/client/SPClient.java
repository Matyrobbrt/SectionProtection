package com.matyrobbrt.sectionprotection.client;

import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.client.SectionProtectionClientAPI;
import com.matyrobbrt.sectionprotection.client.journeymap.ClientJMEventListeners;
import com.matyrobbrt.sectionprotection.network.SPFeatures;
import com.matyrobbrt.sectionprotection.network.SPNetwork;
import com.matyrobbrt.sectionprotection.network.packet.SPPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;

public class SPClient {

    public SPClient(IEventBus modBus) {
        modBus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, SectionProtection.MOD_ID + "-client.toml");
    }

    protected void commonSetup(final FMLCommonSetupEvent event) {
        if (ClientConfig.ENABLE_JOURNEYMAP_INTEGRATION.get() && ModList.get().isLoaded("journeymap")) {
            MinecraftForge.EVENT_BUS.register(ClientJMEventListeners.class);
            SectionProtectionClientAPI.INSTANCE.enableClaimSync();
            SectionProtectionClientAPI.INSTANCE.enableTeamSync();
            MinecraftForge.EVENT_BUS.addListener(ClientJMEventListeners::onPopupEvent);
        }

        event.enqueueWork(() -> {
            // TODO we may want to find a better way of representing sync state.
            // maybe with handshake packets?
            if (ClientApiImpl.claimSync) {
                NetworkRegistry.ChannelBuilder
                        .named(SPNetwork.CHUNK_SYNC_CHANNEL)
                        .networkProtocolVersion(() -> "yes")
                        .clientAcceptedVersions(str -> true)
                        .serverAcceptedVersions(str -> true)
                        .eventNetworkChannel();
            }

            if (ClientApiImpl.teamSync) {
                NetworkRegistry.ChannelBuilder
                        .named(SPNetwork.TEAM_SYNC_CHANNEL)
                        .networkProtocolVersion(() -> "yes")
                        .clientAcceptedVersions(str -> true)
                        .serverAcceptedVersions(str -> true)
                        .eventNetworkChannel();
            }
        });
    }

    public static void sendToServer(SPFeatures feature, SPPacket packet) {
        if (serverCanReceivePacket(feature)) {
            SPNetwork.getChannel(feature).sendToServer(packet);
        }
    }

    public static boolean serverCanReceivePacket(SPFeatures feature) {
        final var connection = Minecraft.getInstance().getConnection();
        if (connection != null && SPNetwork.isModPresent(connection.getConnection())) {
            return feature.currentVersion().compareTo(SPNetwork.getFeatureVersion(connection.getConnection(), feature)) <= 0;
        }
        return false;
    }
}
