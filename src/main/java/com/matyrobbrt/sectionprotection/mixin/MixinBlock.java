package com.matyrobbrt.sectionprotection.mixin;

import com.matyrobbrt.sectionprotection.MixinHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class MixinBlock extends BlockBehaviour {
    public MixinBlock(Properties pProperties) {
        super(pProperties);
    }

    @Inject(
        at = @At("TAIL"),
        method = "setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V"
    )
    @SuppressWarnings("ALL")
    private void sectionprotection$setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack, CallbackInfo ci) {
        if (((Object) this) instanceof LecternBlock) {
            MixinHooks.Lectern.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        }
    }
}
