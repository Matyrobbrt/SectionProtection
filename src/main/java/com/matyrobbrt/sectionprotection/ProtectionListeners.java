package com.matyrobbrt.sectionprotection;

import java.util.function.Function;

import com.matyrobbrt.sectionprotection.api.ClaimedChunk;
import com.matyrobbrt.sectionprotection.util.Constants;
import com.matyrobbrt.sectionprotection.util.Utils;
import com.matyrobbrt.sectionprotection.world.Banners;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ProtectionListeners {

    @SubscribeEvent
    static void onPlaceEvent(final BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            checkCanExecute(event, player);
        }
    }

    @SubscribeEvent
    static void onBreakEvent(final BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            checkCanExecute(event, player);
        }
    }

    @SubscribeEvent
    static void onMod(final BlockEvent.BlockToolModificationEvent event) {
        if (!event.isSimulated() && event.getPlayer() instanceof ServerPlayer player) {
            checkCanExecute(event, player);
        }
    }

    @SubscribeEvent
    static void interact(final PlayerInteractEvent.RightClickBlock event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            checkCanExecute(event, RightClickBlock::getPos, player);
        }
    }

    @SubscribeEvent
    static void interact(final PlayerInteractEvent.RightClickItem event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            checkCanExecute(event, RightClickItem::getPos, player);
        }
    }

    @SubscribeEvent
    static void griefing(final EntityMobGriefingEvent event) {
        if (event.getEntity().level.isClientSide() || event.getEntity() instanceof Villager)
            return;

        final var chunk = event.getEntity().level.getChunkAt(event.getEntity().blockPosition());
        if (ServerConfig.DEFAULT_MOB_GRIEFING_PROTECTED.get().contains(chunk.getPos())) {
            event.setResult(Result.DENY);
            return;
        }
        chunk.getCapability(ClaimedChunk.CAPABILITY)
            .ifPresent(claimed -> {
                if (claimed.getOwningBanner() != null) {
                    event.setResult(Result.DENY);
                }
            });
    }

    private static void checkCanExecute(final BlockEvent event, final ServerPlayer player) {
        checkCanExecute(event, BlockEvent::getPos, player);
    }

    private static <T extends Event> void checkCanExecute(final T event, final Function<T, BlockPos> pos,
        final ServerPlayer player) {
        if (ServerConfig.ALLOW_FAKE_PLAYERS.get() && player instanceof FakePlayer) {
            return;
        }
        if (!player.isCreative()) {
            final var reg = Banners.get(player.server);
            player.level.getChunkAt(pos.apply(event)).getCapability(ClaimedChunk.CAPABILITY).ifPresent(claimed -> {
                if (claimed.getOwningBanner() != null) {
                    final var team = reg.getMembers(claimed.getOwningBanner());
                    if (team != null && !team.contains(player.getUUID())) {
                        cancelWithContainerUpdate(event, player);
                        final MutableComponent playerName = Utils.getOwnerName(player.server, team)
                                .map(g -> new TextComponent(g).withStyle(Constants.WITH_PLAYER_NAME))
                                .orElse(new TextComponent("someone else").withStyle(ChatFormatting.GRAY));
                        player.sendMessage(new TextComponent(
                            "We're sorry, we can't let you do that! This chunk is owned by ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(playerName),
                            ChatType.GAME_INFO, Util.NIL_UUID);
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
