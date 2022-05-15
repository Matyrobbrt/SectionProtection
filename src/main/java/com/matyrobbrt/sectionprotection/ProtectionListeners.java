package com.matyrobbrt.sectionprotection;

import java.util.function.Function;

import com.matyrobbrt.sectionprotection.api.ClaimedChunk;
import com.matyrobbrt.sectionprotection.api.Member.Permission;
import com.matyrobbrt.sectionprotection.world.TeamRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ProtectionListeners {

    @SubscribeEvent
    static void onPlaceEvent(final BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            checkCanExecute(event, player, Permission.PLACE);
        }
    }

    @SubscribeEvent
    static void onBreakEvent(final BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            checkCanExecute(event, player, Permission.BREAK);
        }
    }

    @SubscribeEvent
    static void onMod(final BlockEvent.BlockToolModificationEvent event) {
        if (!event.isSimulated() && event.getPlayer() instanceof ServerPlayer player) {
            checkCanExecute(event, player, Permission.INTERACT);
        }
    }

    @SubscribeEvent
    static void interact(final PlayerInteractEvent.RightClickBlock event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            checkCanExecute(event, RightClickBlock::getPos, player, Permission.INTERACT);
        }
    }

    @SubscribeEvent
    static void interact(final PlayerInteractEvent.RightClickItem event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            checkCanExecute(event, RightClickItem::getPos, player, Permission.INTERACT);
        }
    }

    private static void checkCanExecute(final BlockEvent event, final ServerPlayer player,
        final Permission permission) {
        checkCanExecute(event, BlockEvent::getPos, player, permission);
    }

    private static <T extends Event> void checkCanExecute(final T event, final Function<T, BlockPos> pos,
        final ServerPlayer player,
        final Permission permission) {
        if (!player.isCreative()) {
            final var reg = TeamRegistry.get(player.getServer());
            player.level.getChunkAt(pos.apply(event)).getCapability(ClaimedChunk.CAPABILITY).ifPresent(claimed -> {
                if (claimed.getOwnerTeam() != null) {
                    final var team = reg.getTeam(claimed.getOwnerTeam());
                    if (team != null) {
                        final var member = team.getMember(player.getUUID());
                        if ((member == null || team.getDefaultPermissions().contains(permission))
                            || !member.getPermissions().contains(permission)) {
                            cancelWithContainerUpdate(event, player);
                        }
                    }
                }
            });
        }
    }

    private static void cancelWithContainerUpdate(final Event event, final ServerPlayer player) {
        event.setCanceled(true);
        player.inventoryMenu.sendAllDataToRemote();
    }

}
