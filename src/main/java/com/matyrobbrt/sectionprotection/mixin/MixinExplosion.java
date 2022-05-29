package com.matyrobbrt.sectionprotection.mixin;

import com.matyrobbrt.sectionprotection.util.ServerConfig;
import com.matyrobbrt.sectionprotection.world.ClaimedChunks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
        final var chunk = new ChunkPos(pPos);
        if (ServerConfig.DEFAULT_EXPLOSION_PROTECTED.get().contains(chunk)) {
            return false;
        }

        // Chunk is claimed... don't explode pls
        return !ClaimedChunks.get(level).isOwned(chunk);
    }
}