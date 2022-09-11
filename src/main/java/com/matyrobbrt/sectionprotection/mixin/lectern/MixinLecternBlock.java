package com.matyrobbrt.sectionprotection.mixin.lectern;

import static net.minecraft.world.level.block.LecternBlock.resetBookState;
import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.util.ServerConfig;
import com.matyrobbrt.sectionprotection.api.extensions.LecternExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LecternBlock.class)
public abstract class MixinLecternBlock extends Block {

    public MixinLecternBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Inject(
        at = @At("HEAD"),
        cancellable = true,
        method = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"
    )
    @SuppressWarnings("ALL")
    private void sectionprotection$use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable<InteractionResult> cir) {
        if (pLevel.isClientSide())
            return;
        pLevel.getBlockEntity(pPos, BlockEntityType.LECTERN).ifPresent(lectern -> {
            final var extensionLectern = ((LecternExtension) lectern);
            final var stack = pPlayer.getItemInHand(pHand);
            if (!extensionLectern.isProtectionLectern() && SectionProtection.isConversionItem(stack)) {
                if (!pPlayer.isCreative() && ServerConfig.CONSUME_CONVERSION_ITEM.get()) {
                    stack.shrink(1);
                }
                extensionLectern.setProtectionLectern(true);
                cir.setReturnValue(InteractionResult.CONSUME);
                pPlayer.displayClientMessage(Component.literal("The Lectern has been converted to a Protection Lectern"), true);
            }
        });
    }

    /**
     * TODO this should be a redirect
     * @reason Because Mojank never calls the {@link LecternBlockEntity#setBook(ItemStack, Player)} method
     * with the player, even if the player is available.
     * @author matyrobbrt - SectionProtection
     */
    @Overwrite
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
