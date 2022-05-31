package com.matyrobbrt.sectionprotection.api.event;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * This event is fired on the Forge Event Bus before
 * {@link net.minecraft.world.level.block.state.BlockState#attack(Level, BlockPos, Player)} is called.
 */
@Cancelable
public class AttackBlockEvent extends PlayerEvent {

    private final InteractionHand hand;
    private final BlockPos pos;

    public AttackBlockEvent(Player player, InteractionHand hand, BlockPos pos) {
        super(Preconditions.checkNotNull(player, "Null player in PlayerInteractEvent!"));
        this.hand = Preconditions.checkNotNull(hand, "Null hand in PlayerInteractEvent!");
        this.pos = Preconditions.checkNotNull(pos, "Null position in PlayerInteractEvent!");
    }

    public BlockPos getPos() {
        return pos;
    }

    public InteractionHand getHand() {
        return hand;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    public Level getLevel() {
        return getPlayer().level;
    }

    public ItemStack getItemStack() {
        return getPlayer().getItemInHand(hand);
    }
}
