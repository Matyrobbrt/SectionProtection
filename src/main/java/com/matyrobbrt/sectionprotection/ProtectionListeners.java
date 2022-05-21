package com.matyrobbrt.sectionprotection;

import com.matyrobbrt.sectionprotection.util.Constants;
import com.matyrobbrt.sectionprotection.util.Utils;
import com.matyrobbrt.sectionprotection.world.Banners;
import com.matyrobbrt.sectionprotection.world.ClaimedChunks;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.function.Function;

public class ProtectionListeners {

    @SubscribeEvent
    static void onPlaceEvent(final BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getPlacedBlock().is(SPTags.ALLOW_PLACING))
                return;
            checkCanExecute(event, player);
        }
    }

    @SubscribeEvent
    static void onBreakEvent(final BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            checkCanExecute(event, player, SPTags.ALLOW_BREAKING);
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
            checkCanExecute(event, RightClickBlock::getPos, player, SPTags.ALLOW_INTERACTION);
        }
    }

    @SubscribeEvent
    static void griefing(final EntityMobGriefingEvent event) {
        if (event.getEntity() == null || event.getEntity().level.isClientSide() ||
            event.getEntity() instanceof Villager ||
            event.getEntity() instanceof Piglin
        )
            return;

        final var chunk = new ChunkPos(event.getEntity().blockPosition());
        if (ServerConfig.DEFAULT_MOB_GRIEFING_PROTECTED.get().contains(chunk)) {
            event.setResult(Result.DENY);
            return;
        }
        if (ClaimedChunks.get(event.getEntity().level).isOwned(chunk))
            event.setResult(Result.DENY);
    }

    private static void checkCanExecute(final BlockEvent event, final ServerPlayer player) {
        checkCanExecute(event, BlockEvent::getPos, player);
    }

    private static void checkCanExecute(final BlockEvent event, final ServerPlayer player, final TagKey<Block> tag) {
        checkCanExecute(event, BlockEvent::getPos, player, tag);
    }

    private static <T extends Event> void checkCanExecute(final T event, final Function<T, BlockPos> pos,
                                                          final ServerPlayer player) {
        checkCanExecute(event, pos, player, null);
    }

    private static <T extends Event> void checkCanExecute(final T event, final Function<T, BlockPos> pos,
                                                          final ServerPlayer player, @Nullable TagKey<Block> tag) {
        if (ServerConfig.ALWAYS_ALLOW_FAKE_PLAYERS.get() && player instanceof FakePlayer) {
            return;
        }
        if (!player.isCreative()) {
            final var posValue = pos.apply(event);
            final var reg = Banners.get(player.server);
            final var manager = ClaimedChunks.get(player.level);
            final var owner = manager.getOwner(posValue);
            if (owner != null) {
                final var team = reg.getMembers(owner.banner());
                if (tag != null && player.level.getBlockState(posValue).is(tag))
                    return;
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
        }
    }

    private static void cancelWithContainerUpdate(final Event event, final ServerPlayer player) {
        event.setCanceled(true);
        player.inventoryMenu.sendAllDataToRemote();
    }

}
