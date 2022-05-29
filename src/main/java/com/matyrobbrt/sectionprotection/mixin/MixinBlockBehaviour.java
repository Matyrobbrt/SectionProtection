package com.matyrobbrt.sectionprotection.mixin;

import com.matyrobbrt.sectionprotection.MixinHooks;
import com.matyrobbrt.sectionprotection.api.event.AttackBlockEvent;
import com.matyrobbrt.sectionprotection.util.ServerConfig;
import com.matyrobbrt.sectionprotection.world.ClaimedChunks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
        if (((Object) this) instanceof AbstractBannerBlock bannerBlock) {
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
        } else if (((Object) this) instanceof AbstractBannerBlock) {
            cir.setReturnValue(MixinHooks.BannerStuff.getDrops(pState, pBuilder, cir.getReturnValue()));
        }
    }

    @Mixin(BlockBehaviour.BlockStateBase.class)
    public static abstract class BlockStateBase {
        @Shadow
        public abstract Block getBlock();

        @Inject(
            at = @At("HEAD"),
            cancellable = true,
            method = "getMapColor"
        )
        private void sectionprotection$highlightClaimedChunks(BlockGetter pLevel, BlockPos pPos, CallbackInfoReturnable<MaterialColor> cir) {
            if (ServerConfig.HIGHLIGHT_MAP.get() && pLevel instanceof Level level && !level.isClientSide()) {
                final var data = ClaimedChunks.get(level);
                final var chunkPos = new ChunkPos(pPos);
                final var owner = data.getOwner(chunkPos);
                if (owner != null) {
                    // Chunk is claimed, now we want to only highlight borders
                    if (
                        pPos.getZ() == chunkPos.getMinBlockZ() ||
                        pPos.getZ() == chunkPos.getMaxBlockZ() ||
                        pPos.getZ() % 16 == 0                  ||
                        pPos.getX() == chunkPos.getMaxBlockX() ||
                        pPos.getX() == chunkPos.getMaxBlockX() ||
                        pPos.getX() % 16 == 0
                    )
                        cir.setReturnValue(MaterialColor.FIRE);
                }
            }
        }

        @Inject(
            at = @At("HEAD"),
            cancellable = true,
            method = "attack"
        )
        private void sectionprotection$attack(Level pLevel, BlockPos pPos, Player pPlayer, CallbackInfo ci) {
            if (MinecraftForge.EVENT_BUS.post(new AttackBlockEvent(pPlayer, InteractionHand.MAIN_HAND, pPos))) {
                ci.cancel();
            }
        }
    }
}
