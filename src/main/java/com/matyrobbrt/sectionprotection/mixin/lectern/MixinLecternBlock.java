package com.matyrobbrt.sectionprotection.mixin.lectern;

import static net.minecraft.world.level.block.LecternBlock.resetBookState;
import com.matyrobbrt.sectionprotection.Constants;
import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.Banner;
import com.matyrobbrt.sectionprotection.api.BannerExtension;
import com.matyrobbrt.sectionprotection.api.ClaimedChunk;
import com.matyrobbrt.sectionprotection.api.LecternExtension;
import com.matyrobbrt.sectionprotection.util.Utils;
import com.matyrobbrt.sectionprotection.world.Banners;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(LecternBlock.class)
public abstract class MixinLecternBlock extends Block {
    public MixinLecternBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (pLevel.isClientSide) {
            return;
        }
        if (pStack.getOrCreateTag().contains(Constants.PROTECTION_LECTERN)) {
            pLevel.getBlockEntity(pPos, BlockEntityType.LECTERN).ifPresent(lectern -> {
                ((LecternExtension) lectern)
                        .setProtectionLectern(pStack.getOrCreateTag().getBoolean(Constants.PROTECTION_LECTERN));
            });
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        final var sup = super.getDrops(pState, pBuilder);
        final var be = pBuilder.getParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof LecternExtension ext && !sup.isEmpty()) {
            final var stack = sup.get(0);
            stack.getOrCreateTag().putBoolean(Constants.PROTECTION_LECTERN, ext.isProtectionLectern());
            Utils.setLore(stack, new TextComponent("Protection Lectern").withStyle(ChatFormatting.AQUA));
        }
        return sup;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide()) return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
        final var ret = new AtomicReference<InteractionResult>();
        pLevel.getBlockEntity(pPos, BlockEntityType.LECTERN).ifPresent(lectern -> {
            final var extensionLectern = ((LecternExtension) lectern);
            final var stack = pPlayer.getItemInHand(pHand);
            // TODO this needs a tag
            if (!extensionLectern.isProtectionLectern() && stack.is(SectionProtection.IS_CONVERSION_ITEM)) {
                stack.shrink(1);
                extensionLectern.setProtectionLectern(true);
                ret.set(InteractionResult.CONSUME);
            }
        });
        if (ret.get() != null) {
            return ret.get();
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    /**
     * TODO this should be a redirect
     * @author matyrobbrt - SectionProtection
     */
    @Overwrite(aliases = "Lnet/minecraft/world/level/block/LecternBlock;placeBook(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/item/ItemStack;)V")
    private static void placeBook(@javax.annotation.Nullable Player pPlayer, Level pLevel, BlockPos pPos, BlockState pState, ItemStack pBook) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof LecternBlockEntity lecternblockentity) {
            lecternblockentity.setBook(pBook.split(1), pPlayer);
            resetBookState(pLevel, pPos, pState, true);
            pLevel.playSound(null, pPos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);
            pLevel.gameEvent(pPlayer, GameEvent.BLOCK_CHANGE, pPos);
        }
    }
}
