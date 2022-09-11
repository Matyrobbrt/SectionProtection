package com.matyrobbrt.sectionprotection;

import com.matyrobbrt.sectionprotection.api.ActionType;
import com.matyrobbrt.sectionprotection.api.event.AttackBlockEvent;
import com.matyrobbrt.sectionprotection.util.Constants;
import com.matyrobbrt.sectionprotection.util.ServerConfig;
import com.matyrobbrt.sectionprotection.util.Utils;
import com.matyrobbrt.sectionprotection.world.Banners;
import com.matyrobbrt.sectionprotection.world.ClaimedChunks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Function;

public class ProtectionListeners {

    @SubscribeEvent
    static void onPlaceEvent(final BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getPlacedBlock().is(ActionType.PLACING.getTag(Registry.BLOCK_REGISTRY)))
                return;
            if (checkPredicates(APIImpl.getPredicates(ActionType.PLACING), ActionType.PLACING,
                    ctx -> ctx.canPlace(player, event.getBlockSnapshot(), event.getPlacedAgainst())))
                return;
            checkCanExecute(event, player);
        }
    }

    @SubscribeEvent
    static void onBreakEvent(final BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            checkCanExecute(event, player, ActionType.BREAKING,
                    ctx -> ctx.canBreak(player, (Level) event.getLevel(), event.getPos(), event.getState()));
        }
    }

    @SubscribeEvent
    static void onMod(final BlockEvent.BlockToolModificationEvent event) {
        if (!event.isSimulated() && event.getPlayer() instanceof ServerPlayer player) {
            checkCanExecute(event, player);
        }
    }

    @SubscribeEvent
    static void trample(final BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            checkCanExecute(event, player);
        }
    }

    @SubscribeEvent
    static void interact(final RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getItemStack().getItem() instanceof BlockItem)
                return; // Block item place is considered a right click event
            checkCanExecute(event, RightClickBlock::getPos, player, ActionType.BLOCK_INTERACTION,
                    ctx -> ctx.canInteract(player, ActionType.InteractionContext.InteractionType.RIGHT_CLICK,
                            event.getHand(), event.getLevel(), event.getPos(), event.getLevel().getBlockState(event.getPos())),
                    true);
        }
    }

    @SubscribeEvent
    static void attack(final AttackBlockEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            checkCanExecute(event, AttackBlockEvent::getPos, player, ActionType.BLOCK_INTERACTION,
                    ctx -> ctx.canInteract(player, ActionType.InteractionContext.InteractionType.LEFT_CLICK,
                            event.getHand(), event.getLevel(), event.getPos(), event.getLevel().getBlockState(event.getPos())),
                    false);
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

    @SuppressWarnings("SameParameterValue")
    private static <T> void checkCanExecute(final BlockEvent event, final ServerPlayer player, final ActionType<T> type, Function<T, ActionType.Result> checker) {
        checkCanExecute(event, BlockEvent::getPos, player, type, checker, true);
    }

    private static <T extends Event> void checkCanExecute(final T event, final Function<T, BlockPos> pos,
                                                          final ServerPlayer player) {
        checkCanExecute(event, pos, player, null, null, true);
    }

    private static <E extends Event, T> void checkCanExecute(final E event, final Function<E, BlockPos> pos,
                                                            final ServerPlayer player, @Nullable ActionType<T> type,
                                                            @Nullable Function<T, ActionType.Result> checker,
                                                             boolean sendFeedback) {
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
                if (type != null && player.level.getBlockState(posValue).is(type.getTag(Registry.BLOCK_REGISTRY)))
                    return;
                if (team != null && !team.contains(player.getUUID())) {
                    if (type != null && checker != null && checkPredicates(APIImpl.getPredicates(type), type, checker))
                        return;
                    cancelWithContainerUpdate(event, player);
                    if (sendFeedback) {
                        final MutableComponent playerName = Utils.getOwnerName(player.server, team)
                                .map(g -> Component.literal(g).withStyle(Constants.WITH_PLAYER_NAME))
                                .orElse(Component.literal("someone else").withStyle(ChatFormatting.GRAY));
                        player.displayClientMessage(Component.literal(
                                        "We're sorry, we can't let you do that! This chunk is owned by ")
                                        .withStyle(ChatFormatting.GRAY)
                                        .append(playerName), true);
                    }
                }
            }
        }
    }

    private static <T> boolean checkPredicates(Collection<Object> predicates, ActionType<T> type, Function<T, ActionType.Result> checker) {
        for (final var pred : predicates) {
            final var result = checker.apply(type.contextClass().cast(pred));
            if (result == ActionType.Result.ALLOW)
                return true;
            else if (result == ActionType.Result.DENY)
                return false;
        }
        return false;
    }

    private static void cancelWithContainerUpdate(final Event event, final ServerPlayer player) {
        event.setCanceled(true);
        player.inventoryMenu.sendAllDataToRemote();
    }

}
