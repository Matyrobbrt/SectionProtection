package com.matyrobbrt.sectionprotection.mixin;

import com.matyrobbrt.sectionprotection.MixinHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BlockBehaviour.class)
public abstract class MixinBlockBehaviour {

    @Inject(
        at = @At("HEAD"),
        cancellable = true,
        method = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"
    )
    @SuppressWarnings("ALL")
    private void sectionprotection$use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable<InteractionResult> cir) {
        if (((Object) this) instanceof LecternBlock lecternBlock) {
            MixinHooks.Lectern.use(lecternBlock, pState, pLevel, pPos, pPlayer, pHand, pHit, cir);
        } else if (((Object) this) instanceof BannerBlock bannerBlock) {
            MixinHooks.BannerStuff.use(pState, pLevel, pPos, pPlayer, pHand, pHit, cir);
        }
    }

    @Inject(
        at = @At("RETURN"),
        cancellable = true,
        method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/storage/loot/LootContext$Builder;)Ljava/util/List;"
    )
    @SuppressWarnings("ALL")
    private void sectionprotection$getDrops(BlockState pState, LootContext.Builder pBuilder, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (((Object) this) instanceof LecternBlock) {
            cir.setReturnValue(MixinHooks.Lectern.getDrops(pState, pBuilder, cir.getReturnValue()));
        } else if (((Object) this) instanceof BannerBlock) {
            cir.setReturnValue(MixinHooks.BannerStuff.getDrops(pState, pBuilder, cir.getReturnValue()));
        }
    }
}
