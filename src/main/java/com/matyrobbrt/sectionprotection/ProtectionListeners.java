package com.matyrobbrt.sectionprotection;

import com.matyrobbrt.sectionprotection.api.ClaimedChunk;
import com.matyrobbrt.sectionprotection.api.Member.Permission;
import com.matyrobbrt.sectionprotection.world.TeamRegistry;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ProtectionListeners {

    @SubscribeEvent
    static void onPlaceEvent(final BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.isCreative()) {
            final var reg = TeamRegistry.get(player.getServer());
            player.level.getChunkAt(event.getPos()).getCapability(ClaimedChunk.CAPABILITY).ifPresent(claimed -> {
                if (claimed.getOwnerTeam() != null) {
                    final var team = reg.getTeam(claimed.getOwnerTeam());
                    if (team != null) {
                        final var member = team.getMember(player.getUUID());
                        if (member == null || !member.getPermissions().contains(Permission.PLACE)) {
                            cancelWithContainerUpdate(event, player);
                        }
                    }
                }
            });
        }
    }

    static void cancelWithContainerUpdate(final BlockEvent event, final ServerPlayer player) {
        event.setCanceled(true);
        player.inventoryMenu.sendAllDataToRemote();
    }

}
