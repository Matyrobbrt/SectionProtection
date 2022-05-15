package com.matyrobbrt.sectionprotection.mixin.banner;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.matyrobbrt.sectionprotection.Constants;
import com.matyrobbrt.sectionprotection.api.Banner;
import com.matyrobbrt.sectionprotection.api.BannerExtension;
import com.matyrobbrt.sectionprotection.api.ClaimedChunk;
import com.matyrobbrt.sectionprotection.world.Banners;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(AbstractBannerBlock.class)
public abstract class MixinBannerBlock extends Block {

    public MixinBannerBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @SuppressWarnings("static-method")
    @Inject(at = @At("TAIL"), method = "setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V")
    private void sectionprotection$setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer,
        ItemStack pStack, CallbackInfo ci) {
        if (pLevel.isClientSide) {
            return;
        }
        if (pStack.getOrCreateTag().contains(Constants.PROTECTION_BANNER)) {
            pLevel.getBlockEntity(pPos, BlockEntityType.BANNER).ifPresent(banner -> {
                ((BannerExtension) banner)
                    .setProtectionBanner(pStack.getOrCreateTag().getBoolean(Constants.PROTECTION_BANNER));
            });
        }
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide()) return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
        final var ret = new AtomicReference<InteractionResult>();
        pLevel.getChunkAt(pPos).getCapability(ClaimedChunk.CAPABILITY).ifPresent(cap -> {
            if (cap.getOwningBanner() == null) {
                pLevel.getBlockEntity(pPos, BlockEntityType.BANNER).ifPresent(banner -> {
                    final var extensionBanner = ((BannerExtension) banner);
                    final var stack = pPlayer.getItemInHand(pHand);
                    final var pattern = new Banner(banner.getPatterns().stream().map(p -> new Banner.Data(p.getSecond(), p.getFirst())).toList());
                    if (!extensionBanner.isProtectionBanner() && stack.is(SectionProtection.IS_CONVERSION_ITEM)) {
                        final var banners = Banners.get(Objects.requireNonNull(pLevel.getServer()));
                        final var team = banners.getMembers(pattern);
                        if (team != null) {
                            if (team.contains(pPlayer.getUUID())) {
                                cap.setOwningBanner(pattern);
                                extensionBanner.setProtectionBanner(true);
                                stack.shrink(1);
                                ret.set(InteractionResult.CONSUME);
                            }
                        } else {
                            banners.createTeam(pattern, pPlayer.getUUID());
                            cap.setOwningBanner(pattern);
                            extensionBanner.setProtectionBanner(true);
                            stack.shrink(1);
                            pPlayer.sendMessage(new TextComponent("Created new team!").withStyle(ChatFormatting.GRAY), Util.NIL_UUID);
                            ret.set(InteractionResult.CONSUME);
                        }
                    }
                });
            }
        });
        if (ret.get() != null) {
            return ret.get();
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        final var sup = super.getDrops(pState, pBuilder);
        final var be = pBuilder.getParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof BannerExtension ext && !sup.isEmpty()) {
            final var stack = sup.get(0);
            stack.getOrCreateTag().putBoolean(Constants.PROTECTION_BANNER, ext.isProtectionBanner());
            Utils.setLore(stack, new TextComponent("Protection Banner").withStyle(ChatFormatting.AQUA));
        }
        return sup;
    }

}
