package com.matyrobbrt.sectionprotection;

import com.matyrobbrt.sectionprotection.api.Banner;
import com.matyrobbrt.sectionprotection.api.BannerExtension;
import com.matyrobbrt.sectionprotection.api.LecternExtension;
import com.matyrobbrt.sectionprotection.util.Constants;
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
        if (item.getBlock() instanceof net.minecraft.world.level.block.BannerBlock bannerBlock && pContext.getItemInHand().getOrCreateTag().contains(Constants.PROTECTION_BANNER) && !pContext.getLevel().isClientSide()) {
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
            final var cantClaim = ServerConfig.getChunksToClaim(pos)
                .anyMatch(data::isOwned);
            if (cantClaim) {
                // Somebody owns it already
                if (pContext.getPlayer() != null) {
                    pContext.getPlayer().displayClientMessage(new TextComponent("It seems like the chunks you are about to claim are already claimed!")
                            .withStyle(ChatFormatting.RED), true);
                    if (pContext.getPlayer() instanceof ServerPlayer serverPlayer) {
                        serverPlayer.inventoryMenu.sendAllDataToRemote();
                    }
                }
                cir.setReturnValue(InteractionResult.FAIL);
                return;
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
        public static void use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable<InteractionResult> cir) {
            if (pLevel.isClientSide() || pPlayer instanceof FakePlayer)
                return;
            final var chunk = new ChunkPos(pPos);
            final var toClaim = ServerConfig.getChunksToClaim(chunk).toList();
            final var claimedData = ClaimedChunks.get(pLevel);
            if (toClaim.stream().anyMatch(c -> claimedData.isOwned(c) || !SectionProtection.canClaimChunk(pPlayer, c)))
                return;
            pLevel.getBlockEntity(pPos, BlockEntityType.BANNER).ifPresent(banner -> {
                final var extensionBanner = ((BannerExtension) banner);
                final var stack = pPlayer.getItemInHand(pHand);
                final var pattern = com.matyrobbrt.sectionprotection.api.Banner.from(banner.getPatterns());
                if (
                    !extensionBanner.isProtectionBanner() &&
                    SectionProtection.isConversionItem(stack)
                ) {
                    final var banners = Banners.get(Objects.requireNonNull(pLevel.getServer()));
                    final var team = banners.getMembers(pattern);
                    if (team != null) {
                        if (team.contains(pPlayer.getUUID())) {
                            toClaim.forEach(c -> claimedData.setOwner(c, pattern));
                            extensionBanner.setProtectionBanner(true);
                            if (!pPlayer.isCreative()) {
                                stack.shrink(1);
                            }
                            cir.setReturnValue(InteractionResult.CONSUME);
                        }
                    } else {
                        banners.createTeam(pattern, pPlayer.getUUID());
                        toClaim.forEach(c -> claimedData.setOwner(c, pattern));
                        extensionBanner.setProtectionBanner(true);
                        if (!pPlayer.isCreative()) {
                            stack.shrink(1);
                        }
                        pPlayer.sendMessage(new TextComponent("Created new team!").withStyle(ChatFormatting.GRAY), Util.NIL_UUID);
                        cir.setReturnValue(InteractionResult.CONSUME);
                    }
                }
            });
        }

        public static List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder, List<ItemStack> sup) {
            final var be = pBuilder.getParameter(LootContextParams.BLOCK_ENTITY);
            if (be instanceof BannerExtension ext && !sup.isEmpty()) {
                final var stack = sup.get(0);
                stack.getOrCreateTag().putBoolean(Constants.PROTECTION_BANNER, ext.isProtectionBanner());
                Utils.setLore(stack, new TextComponent("Protection Banner").withStyle(ChatFormatting.AQUA));
            }
            return sup;
        }

        public static void setRemoved(BannerBlockEntity banner) {
            final var ext = ((BannerExtension) banner);
            if (
                // @Volatile: MC calls setRemoved when a chunk unloads now as well (see ServerLevel#unload -> LevelChunk#clearAllBlockEntities).
                // Since we don't want to remove the claimed status of a chunk (which also makes the server freeze in the case of a save in progress), we need to know if it was removed due to unloading.
                // We can use "unloaded" for that, it's set in #onChunkUnloaded.
                // Since MC first calls #onChunkUnloaded and then #setRemoved, this check keeps working.
                !ext.getSectionProtectionIsUnloaded()

                && ext.isProtectionBanner() && banner.getLevel() != null && !banner.getLevel().isClientSide()) {
                final var pattern = Banner.from(banner.getPatterns());
                final var claimData = ClaimedChunks.get(banner.getLevel());
                ServerConfig.getChunksToClaim(new ChunkPos(banner.getBlockPos()))
                    .filter(chunk -> Objects.equals(claimData.getOwner(chunk), pattern))
                    .forEach(claimData::clearOwner);
            }
        }

        public static void onUnloaded(BannerExtension banner) {
            banner.setSectionProtectionIsUnloaded(true);
        }
    }
}
