package com.matyrobbrt.sectionprotection.mixin.banner;

import com.matyrobbrt.sectionprotection.ServerConfig;
import com.matyrobbrt.sectionprotection.api.Banner;
import com.matyrobbrt.sectionprotection.util.Constants;
import com.matyrobbrt.sectionprotection.api.BannerExtension;
import com.matyrobbrt.sectionprotection.world.ClaimedChunks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

//@formatter:off
@Mixin(BannerBlockEntity.class)
public class MixinBannerBE extends BlockEntity implements BannerExtension {

    private boolean isProtectionBanner;
    private boolean sectionprotection$unloaded;

    public MixinBannerBE(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    @Override
    public boolean isProtectionBanner() {
        return isProtectionBanner;
    }
    
    @Override
    public void setProtectionBanner(boolean isProtectionBanner) {
        this.isProtectionBanner = isProtectionBanner;
    }
    
    @Inject(
        method = "saveAdditional(Lnet/minecraft/nbt/CompoundTag;)V",
        at = @At("TAIL")
    )
    private void sectionprotection$saveAdditional(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean(Constants.PROTECTION_BANNER, isProtectionBanner);
    }
    
    @Inject(
        method = "load(Lnet/minecraft/nbt/CompoundTag;)V",
        at = @At("TAIL")
    )
    private void sectionprotection$load(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(Constants.PROTECTION_BANNER)) {
            isProtectionBanner = tag.getBoolean(Constants.PROTECTION_BANNER);
        }
    }
    
    @Inject(method = "getItem()Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void sectionprotection$saveItem(CallbackInfoReturnable<ItemStack> ci, ItemStack stack) {
        stack.getOrCreateTag().putBoolean(Constants.PROTECTION_BANNER, isProtectionBanner);
    }

    @Override
    public boolean getSectionProtectionIsUnloaded() {
        return sectionprotection$unloaded;
    }

    @Override
    public void setSectionProtectionIsUnloaded(boolean isUnloaded) {
        this.sectionprotection$unloaded = isUnloaded;
    }

    @Override
    public void sectionProtectionUnclaim() {
        if (
            // @Volatile: MC calls setRemoved when a chunk unloads now as well (see ServerLevel#unload -> LevelChunk#clearAllBlockEntities).
            // Since we don't want to remove the claimed status of a chunk (which also makes the server freeze in the case of a save in progress), we need to know if it was removed due to unloading.
            // We can use "unloaded" for that, it's set in #onChunkUnloaded.
            // Since MC first calls #onChunkUnloaded and then #setRemoved, this check keeps working.
            !sectionprotection$unloaded
            && isProtectionBanner() && level != null && !level.isClientSide()) {
            final var pattern = Banner.from(((BannerBlockEntity) (Object) this).getPatterns());
            final var claimData = ClaimedChunks.get(level);
            ServerConfig.getChunksToClaim(new ChunkPos(getBlockPos()))
                    .filter(chunk -> {
                        final var owner = claimData.getOwner(chunk);
                        return owner != null && owner.banner().equals(pattern);
                    })
                    .forEach(claimData::clearOwner);
        }
    }
}
