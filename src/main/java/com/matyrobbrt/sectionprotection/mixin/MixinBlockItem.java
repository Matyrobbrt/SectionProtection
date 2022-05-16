package com.matyrobbrt.sectionprotection.mixin;

import com.matyrobbrt.sectionprotection.MixinHooks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem extends Item {

    public MixinBlockItem(Properties pProperties) {
        super(pProperties);
    }

    @Inject(
        at = @At("HEAD"),
        method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;",
        cancellable = true
    )
    private void sectionprotection$checkBanner(UseOnContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
        MixinHooks.checkBlockItemIsBanner((BlockItem) (Object) this, pContext, cir);
    }
}