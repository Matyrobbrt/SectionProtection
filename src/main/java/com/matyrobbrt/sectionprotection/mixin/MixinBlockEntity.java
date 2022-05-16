package com.matyrobbrt.sectionprotection.mixin;

import com.matyrobbrt.sectionprotection.MixinHooks;
import com.matyrobbrt.sectionprotection.api.BannerExtension;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity {

    @Inject(
        at = @At("TAIL"),
        method = "setRemoved()V"
    )
    @SuppressWarnings("ALL")
    private void sectionprotection$setRemoved(CallbackInfo ci) {
        if (((Object) this) instanceof BannerBlockEntity ban) {
            MixinHooks.BannerStuff.setRemoved(ban);
        }
    }

    @Inject(
        at = @At("TAIL"),
        method = "onChunkUnloaded()V",
        remap = false
    )
    private void sectionprotection$unloaded(CallbackInfo ci) {
        if (this instanceof BannerExtension ext) {
            MixinHooks.BannerStuff.onUnloaded(ext);
        }
    }

}
