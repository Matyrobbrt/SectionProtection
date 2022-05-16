package com.matyrobbrt.sectionprotection.mixin.lectern;

import static net.minecraft.world.level.block.LecternBlock.resetBookState;
import com.matyrobbrt.sectionprotection.Constants;
import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.LecternExtension;
import com.matyrobbrt.sectionprotection.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(LecternBlock.class)
public abstract class MixinLecternBlock extends Block {

    public MixinLecternBlock(Properties p_49795_) {
        super(p_49795_);
    }

    /**
     * TODO this should be a redirect
     * Why does this exist? Because Mojank never calls the {@link LecternBlockEntity#setBook(ItemStack, Player)} method
     * with the player, event if the player is available.
     * @author matyrobbrt - SectionProtection
     */
    @Overwrite(aliases = "Lnet/minecraft/world/level/block/LecternBlock;placeBook(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/item/ItemStack;)V")
    private static void placeBook(@javax.annotation.Nullable Player pPlayer, Level pLevel, BlockPos pPos, BlockState pState, ItemStack pBook) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof LecternBlockEntity lecternblockentity) {
            lecternblockentity.setBook(pBook.split(1), pPlayer);
            resetBookState(pLevel, pPos, pState, true);
            pLevel.playSound(null, pPos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);
            pLevel.gameEvent(pPlayer, GameEvent.BLOCK_CHANGE, pPos);
        }
    }
}
