package com.matyrobbrt.sectionprotection.client.journeymap;

import com.matyrobbrt.sectionprotection.api.banner.Banner;
import com.matyrobbrt.sectionprotection.api.event.ChunkDataChangeEvent;
import com.matyrobbrt.sectionprotection.api.event.TeamChangeEvent;
import journeymap.client.api.event.forge.PopupMenuEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLLoader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientJMEventListeners {

    private static final Map<Banner, List<UUID>> STORE = new HashMap<>();

    @SubscribeEvent
    static void logout(final ClientPlayerNetworkEvent.LoggingOut event) {
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
                    event.newData.bannerPos(), event.newData.banner(), isSelfMember
            );
        }
    }

    public static void onPopupEvent(PopupMenuEvent.FullscreenPopupMenuEvent event) {
        if (!FMLLoader.isProduction()) {
            // TODO figure this out
            event.getPopupMenu().addMenuItem("Claim chunk", b -> System.out.printf("Clicked at %s", b));
        }
    }

    @SubscribeEvent
    static void onTeamChange(final TeamChangeEvent.Client event) {
        STORE.put(event.getBanner(), event.getMembers());
    }

    @Nullable
    public static List<UUID> getMembers(final Banner team) {
        return STORE.get(team);
    }
}
