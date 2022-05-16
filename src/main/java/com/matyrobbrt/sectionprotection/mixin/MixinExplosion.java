package com.matyrobbrt.sectionprotection.mixin;

import com.matyrobbrt.sectionprotection.api.ClaimedChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(Explosion.class)
public abstract class MixinExplosion {

    @Shadow @Final private Level level;

    @Redirect(
        method = "explode()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isInWorldBounds(Lnet/minecraft/core/BlockPos;)Z")
    )
    private boolean sectionprotection$canExplodeBlock(Level instance, BlockPos pPos) {
        if (!this.level.isInWorldBounds(pPos)) {
            return false;
        }
        final var canBreak = new AtomicBoolean(true);
        this.level.getChunkAt(pPos).getCapability(ClaimedChunk.CAPABILITY)
                .ifPresent(cap -> {
                    if (cap.getOwningBanner() != null)
                        canBreak.set(false);
                });
        return canBreak.get();
    }
}