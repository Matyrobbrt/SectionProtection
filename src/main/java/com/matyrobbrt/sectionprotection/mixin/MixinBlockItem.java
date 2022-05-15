package com.matyrobbrt.sectionprotection.mixin;

import com.matyrobbrt.sectionprotection.Constants;
import com.matyrobbrt.sectionprotection.api.Banner;
import com.matyrobbrt.sectionprotection.api.ClaimedChunk;
import com.matyrobbrt.sectionprotection.util.Utils;
import com.matyrobbrt.sectionprotection.world.Banners;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem extends Item {
    public MixinBlockItem(Properties pProperties) {
        super(pProperties);
    }

    @Inject(
        at = @At("HEAD"),
        method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;",
        cancellable = true
    )
    private void sectionprotection$checkBanner(UseOnContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
        if (pContext.getItemInHand().getOrCreateTag().contains(Constants.PROTECTION_BANNER) && !pContext.getLevel().isClientSide()) {
            final var banners = Banners.get(Objects.requireNonNull(pContext.getLevel().getServer()));
            final var pattern = new Banner(BannerBlockEntity.getItemPatterns(pContext.getItemInHand()));
            final var team = banners.getMembers(pattern);
            pContext.getLevel().getChunkAt(pContext.getClickedPos()).getCapability(ClaimedChunk.CAPABILITY)
                    .ifPresent(cl -> {
                        if (cl.getOwningBanner() == null)
                            return;
                        if (pContext.getPlayer() != null) {
                            final MutableComponent ownerName = Utils.getOwnerName(pContext.getLevel().getServer(), team)
                                    .map(g -> new TextComponent(g).withStyle(s -> s.withColor(0x009B00)))
                                    .orElse(new TextComponent("someone else"));
                            pContext.getPlayer().displayClientMessage(new TextComponent("It seems like this chunk is claimed by ")
                                    .withStyle(ChatFormatting.RED)
                                    .append(ownerName)
                                    .append(" already!")
                                    .withStyle(ChatFormatting.RED), true);
                            if (pContext.getPlayer() instanceof ServerPlayer serverPlayer) {
                                serverPlayer.inventoryMenu.sendAllDataToRemote();
                            }
                        }
                        cir.setReturnValue(InteractionResult.FAIL);
                    });
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
}