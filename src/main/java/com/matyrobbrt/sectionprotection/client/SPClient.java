package com.matyrobbrt.sectionprotection.client;

import com.matyrobbrt.sectionprotection.api.client.SectionProtectionClientAPI;
import com.matyrobbrt.sectionprotection.client.journeymap.ClientJMEventListeners;
import com.matyrobbrt.sectionprotection.network.SPNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;

public class SPClient {

    public SPClient(IEventBus modBus) {
        if (ModList.get().isLoaded("journeymap")) {
            MinecraftForge.EVENT_BUS.register(ClientJMEventListeners.class);
            SectionProtectionClientAPI.INSTANCE.enableClaimSync();
            // enable once this feature is implemented
            // SectionProtectionClientAPI.INSTANCE.enableTeamSync();
        }

        modBus.addListener(this::registerMarkerChannels);
    }

    protected void registerMarkerChannels(final FMLCommonSetupEvent event) {
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
}
