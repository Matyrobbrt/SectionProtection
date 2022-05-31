package com.matyrobbrt.sectionprotection.mixin.banner;

import com.matyrobbrt.sectionprotection.util.ServerConfig;
import com.matyrobbrt.sectionprotection.util.Constants;
import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.banner.Banner;
import com.matyrobbrt.sectionprotection.api.extensions.BannerExtension;
import com.matyrobbrt.sectionprotection.world.Banners;
import com.matyrobbrt.sectionprotection.world.ClaimedChunks;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(AbstractBannerBlock.class)
public abstract class MixinBannerBlock extends Block {

    public MixinBannerBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @SuppressWarnings("static-method")
    @Inject(at = @At("TAIL"), method = "setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V")
    private void sectionprotection$setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer,
        ItemStack pStack, CallbackInfo ci) {
        if (pLevel.isClientSide && !(pPlacer instanceof FakePlayer)) {
            return;
        }
        if (pStack.getOrCreateTag().contains(Constants.PROTECTION_BANNER)) {
            pLevel.getBlockEntity(pPos, BlockEntityType.BANNER).ifPresent(banner -> {
                final var isProtection = pStack.getOrCreateTag().getBoolean(Constants.PROTECTION_BANNER);
                if (isProtection) {
                    ((BannerExtension) banner).setProtectionBanner(true);
                    if (pPlacer instanceof Player player && !(pPlacer instanceof FakePlayer)) {
                        final var manager = ClaimedChunks.get(pLevel);
                        final var chunks = ServerConfig.getChunksToClaim(new ChunkPos(pPos)).toList();
                        if (chunks.stream().anyMatch(c -> (ServerConfig.ONLY_FULL_CLAIM.get() && manager.isOwned(c)) || !SectionProtection.canClaimChunk(player, c)))
                            return; // Don't bother sending feedback... if they bypassed BlockItem#use then they probably are doing something weird
                        final var banners = Banners.get(Objects.requireNonNull(pLevel.getServer()));
                        final var pattern = Banner.from(banner.getPatterns());
                        final var team = banners.getMembers(pattern);
                        if (team != null) {
                            if (team.contains(player.getUUID())) {
                                chunks.forEach(c -> manager.setOwner(c, pattern, pPos));
                            }
                        } else {
                            banners.createTeam(pattern, player.getUUID());
                            chunks.forEach(c -> manager.setOwner(c, pattern, pPos));
                            player.sendMessage(new TextComponent("Created new team!").withStyle(ChatFormatting.GRAY), Util.NIL_UUID);
                        }
                    }
                }
            });
        }
    }

    @Override // Default method in interface... can't do what we do for `getDrops`
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        var res = super.getExplosionResistance(state, level, pos, explosion);
        final var beOpt = level.getBlockEntity(pos, BlockEntityType.BANNER);
        if (beOpt.isPresent()) {
            final var be = beOpt.get();
            if (((BannerExtension) be).isProtectionBanner()) {
                return 3600000.0F;
            }
        }
        return res;
    }
}
