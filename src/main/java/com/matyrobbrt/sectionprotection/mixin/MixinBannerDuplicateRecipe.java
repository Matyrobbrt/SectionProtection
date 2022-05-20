package com.matyrobbrt.sectionprotection.mixin;

import com.matyrobbrt.sectionprotection.util.Constants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BannerDuplicateRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BannerDuplicateRecipe.class)
public abstract class MixinBannerDuplicateRecipe {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setCount(I)V"), method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;)Lnet/minecraft/world/item/ItemStack;")
    private void sectionprotection$redirectAssemble(ItemStack stack, int oldCount) {
        stack.setCount(1);
        if (stack.getOrCreateTag().contains(Constants.PROTECTION_BANNER)) {
            stack.getOrCreateTag().remove(Constants.PROTECTION_BANNER); // Prevent duplication of protection banners
        }
    }

}
