package com.matyrobbrt.sectionprotection.mixin;

import com.matyrobbrt.sectionprotection.util.FakePlayerHolder;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This is <b>so frowned upon</b>, but unfortunately people don't use FakePlayers as they should.
 * For now, this seems like the best option.
 */
@Mixin(FakePlayer.class)
public abstract class MixinFakePlayer extends ServerPlayer {

    public MixinFakePlayer(MinecraftServer pServer, ServerLevel pLevel, GameProfile pProfile, @Nullable ProfilePublicKey pProfilePublicKey) {
        super(pServer, pLevel, pProfile, pProfilePublicKey);
    }

    @Inject(
        method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;)V",
        at = @At("TAIL")
    )
    private void sectionprotection$holdFakePlayers(ServerLevel level, GameProfile name, CallbackInfo ci) {
        FakePlayerHolder.holdPlayer((FakePlayer) (Object) this);
    }

}
