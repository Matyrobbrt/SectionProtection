package com.matyrobbrt.sectionprotection.client.journeymap;

import com.matyrobbrt.sectionprotection.api.banner.Banner;
import com.matyrobbrt.sectionprotection.api.event.ChunkDataChangeEvent;
import com.matyrobbrt.sectionprotection.api.event.TeamChangeEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientJMEventListeners {

    private static final Map<Banner, List<UUID>> STORE = new HashMap<>();

    @SubscribeEvent
    static void logout(final ClientPlayerNetworkEvent.LoggedOutEvent event) {
        JMHelper.getInstance().removeAllMarkers();
    }

    @SubscribeEvent
    static void onChunkChange(final ChunkDataChangeEvent.Client event) {
        if (event.newData == null) {
            JMHelper.getInstance().unclaim(event.pos, event.dimension);
        } else {
            final boolean isSelfMember;
            if (Minecraft.getInstance().player != null) {
                isSelfMember = STORE.computeIfAbsent(event.getNewOwner(), it -> new ArrayList<>())
                        .contains(Minecraft.getInstance().player.getUUID());
            } else {
                isSelfMember = false;
            }
            JMHelper.getInstance().setClaimed(
                    event.pos, event.dimension, event.newData.banner().data().get(0).color(),
                    event.newData.bannerPos(), isSelfMember
            );
        }
    }

    @SubscribeEvent
    static void onTeamChange(final TeamChangeEvent.Client event) {
        STORE.put(event.getBanner(), event.getMembers());
    }
}
