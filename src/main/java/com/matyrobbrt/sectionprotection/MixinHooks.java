package com.matyrobbrt.sectionprotection;

import com.matyrobbrt.sectionprotection.api.banner.Banner;
import com.matyrobbrt.sectionprotection.api.extensions.BannerExtension;
import com.matyrobbrt.sectionprotection.api.extensions.LecternExtension;
import com.matyrobbrt.sectionprotection.util.Constants;
import com.matyrobbrt.sectionprotection.util.ServerConfig;
import com.matyrobbrt.sectionprotection.util.Utils;
import com.matyrobbrt.sectionprotection.world.Banners;
import com.matyrobbrt.sectionprotection.world.ClaimedChunks;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

public class MixinHooks {

    public static void checkBlockItemIsBanner(BlockItem item, UseOnContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
        if (item.getBlock() instanceof net.minecraft.world.level.block.BannerBlock bannerBlock && pContext.getItemInHand().getOrCreateTag().getBoolean(Constants.PROTECTION_BANNER) && !pContext.getLevel().isClientSide()) {
            final var pos = new ChunkPos(pContext.getClickedPos());
            if (!SectionProtection.canClaimChunk(pContext.getPlayer(), pos)) {
                cir.setReturnValue(InteractionResult.FAIL);
                if (pContext.getPlayer() != null) {
                    pContext.getPlayer().containerMenu.sendAllDataToRemote();
                }
                return;
            }
            final var banners = Banners.get(Objects.requireNonNull(pContext.getLevel().getServer()));
            final var pattern = new Banner(BannerBlockEntity.getItemPatterns(pContext.getItemInHand()), bannerBlock.getColor());
            final var team = banners.getMembers(pattern);
            final var data = ClaimedChunks.get(pContext.getLevel());
            final var toClaim = ServerConfig.getChunksToClaim(pos);
            for (final var it = toClaim.iterator(); it.hasNext();) {
                final var sub = it.next();
                if (!SectionProtection.canClaimChunk(pContext.getPlayer(), pos)) {
                    cir.setReturnValue(InteractionResult.FAIL);
                    if (pContext.getPlayer() != null) {
                        pContext.getPlayer().containerMenu.sendAllDataToRemote();
                    }
                    return;
                } else if (ServerConfig.ONLY_FULL_CLAIM.get() && data.isOwned(sub)) {
                    cir.setReturnValue(InteractionResult.FAIL);
                    if (pContext.getPlayer() != null) {
                        pContext.getPlayer().displayClientMessage(new TextComponent("The chunk at ")
                                .append(new TextComponent(sub.getMiddleBlockPosition(64).toShortString()).withStyle(ChatFormatting.BLUE))
                                .append(" is claimed already!").withStyle(ChatFormatting.RED), true);
                        pContext.getPlayer().containerMenu.sendAllDataToRemote();
                    }
                    return;
                }
            }
            if (pContext.getPlayer() == null || (
                    team != null && !team.contains(pContext.getPlayer().getUUID()))
            ) {
                cir.setReturnValue(InteractionResult.FAIL);
                if (pContext.getPlayer() instanceof ServerPlayer serverPlayer) {
                    serverPlayer.inventoryMenu.sendAllDataToRemote();
                    final MutableComponent ownerName = Utils.getOwnerName(pContext.getLevel().getServer(), team)
                            .map(g -> new TextComponent(g).withStyle(s -> s.withColor(0x009B00)))
                            .orElse(new TextComponent("someone else"));
                    pContext.getPlayer().displayClientMessage(new TextComponent("This pattern is owned by ")
                            .withStyle(ChatFormatting.RED)
                            .append(ownerName), true);
                }
            }
        }
    }

    public static final class Lectern {

        public static List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder, List<ItemStack> sup) {
            final var be = pBuilder.getParameter(LootContextParams.BLOCK_ENTITY);
            if (be instanceof LecternExtension ext && !sup.isEmpty()) {
                final var stack = sup.get(0);
                stack.getOrCreateTag().putBoolean(Constants.PROTECTION_LECTERN, ext.isProtectionLectern());
				if (ext.isProtectionLectern())
					Utils.setLore(stack, new TextComponent("Protection Lectern").withStyle(ChatFormatting.AQUA));
            }
            return sup;
        }

        public static void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
            if (pLevel.isClientSide) {
                return;
            }
            if (pStack.getOrCreateTag().contains(Constants.PROTECTION_LECTERN)) {
                pLevel.getBlockEntity(pPos, BlockEntityType.LECTERN).ifPresent(lectern -> ((LecternExtension) lectern)
                        .setProtectionLectern(pStack.getOrCreateTag().getBoolean(Constants.PROTECTION_LECTERN)));
            }
        }
    }

    public static final class BannerStuff {
        public static void use(Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable<InteractionResult> cir) {
            if (pLevel.isClientSide() || pPlayer instanceof FakePlayer)
                return;
            final var stack = pPlayer.getItemInHand(pHand);
            if (!SectionProtection.isConversionItem(stack)) return;
            final var chunk = new ChunkPos(pPos);
            final var toClaim = ServerConfig.getChunksToClaim(chunk).toList();
            final var claimedData = ClaimedChunks.get(pLevel);
            for (final var subPos : toClaim) {
                if (!SectionProtection.canClaimChunk(pPlayer, subPos)) {
                    pPlayer.containerMenu.sendAllDataToRemote();
                    cir.setReturnValue(InteractionResult.FAIL);
                    return;
                } else if ((ServerConfig.ONLY_FULL_CLAIM.get() || subPos.equals(chunk)) && claimedData.isOwned(subPos)) {
                    pPlayer.displayClientMessage(new TextComponent("The chunk at ")
                            .append(new TextComponent(subPos.getMiddleBlockPosition(64).toShortString()).withStyle(ChatFormatting.BLUE))
                            .append(" is claimed already!").withStyle(ChatFormatting.RED), true);
                    pPlayer.containerMenu.sendAllDataToRemote();
                    cir.setReturnValue(InteractionResult.FAIL);
                    return;
                }
            }
            pLevel.getBlockEntity(pPos, BlockEntityType.BANNER).ifPresent(banner -> {
                final var extensionBanner = ((BannerExtension) banner);
                final var pattern = Banner.from(banner.getPatterns());
                if (pattern.equals(Constants.OMINOUS)) {
                    pPlayer.sendMessage(new TextComponent("Sorry, but Ominous Banners cannot be converted into Protection Banners."), Util.NIL_UUID);
                    return;
                }
                if (!extensionBanner.isProtectionBanner()) {
                    final var banners = Banners.get(Objects.requireNonNull(pLevel.getServer()));
                    final var team = banners.getMembers(pattern);
                    if (team != null) {
                        if (team.contains(pPlayer.getUUID())) {
                            toClaim.forEach(c -> {
                                if (!claimedData.isOwned(c))
                                    claimedData.setOwner(c, pattern, pPos);
                            });
                            extensionBanner.setProtectionBanner(true);
                            if (!pPlayer.isCreative() && ServerConfig.CONSUME_CONVERSION_ITEM.get()) {
                                stack.shrink(1);
                            }
                            cir.setReturnValue(InteractionResult.PASS);
                            pPlayer.displayClientMessage(new TextComponent("The Banner has been converted to a Protection Banner"), true);
                        }
                    } else {
                        banners.createTeam(pattern, pPlayer.getUUID());
                        toClaim.forEach(c -> {
                            if (!claimedData.isOwned(c))
                                claimedData.setOwner(c, pattern, pPos);
                        });
                        extensionBanner.setProtectionBanner(true);
                        if (!pPlayer.isCreative() && ServerConfig.CONSUME_CONVERSION_ITEM.get()) {
                            stack.shrink(1);
                        }
                        pPlayer.sendMessage(new TextComponent("Created new team!").withStyle(ChatFormatting.GRAY), Util.NIL_UUID);
                        cir.setReturnValue(InteractionResult.PASS);
                        pPlayer.displayClientMessage(new TextComponent("The Banner has been converted to a Protection Banner"), true);
                    }
                }
            });
        }

        public static List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder, List<ItemStack> sup) {
            final var be = pBuilder.getParameter(LootContextParams.BLOCK_ENTITY);
            if (be instanceof BannerExtension ext && !sup.isEmpty()) {
                final var stack = sup.get(0);
                stack.getOrCreateTag().putBoolean(Constants.PROTECTION_BANNER, ext.isProtectionBanner());
				if (ext.isProtectionBanner())
					Utils.setLore(stack, new TextComponent("Protection Banner").withStyle(ChatFormatting.AQUA));
            }
            return sup;
        }

        public static void setRemoved(BannerBlockEntity banner) {
             ((BannerExtension) banner).sectionProtectionUnclaim();
        }

        public static void onUnloaded(BannerExtension banner) {
             banner.setSectionProtectionIsUnloaded(true);
        }
    }
}
