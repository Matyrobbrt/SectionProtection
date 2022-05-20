package com.matyrobbrt.sectionprotection.mixin;

import com.matyrobbrt.sectionprotection.util.FakePlayerHolder;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
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

    public MixinFakePlayer(MinecraftServer p_143384_, ServerLevel p_143385_, GameProfile p_143386_) {
        super(p_143384_, p_143385_, p_143386_);
    }

    @Inject(
        method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;)V",
        at = @At("TAIL")
    )
    private void sectionprotection$holdFakePlayers(ServerLevel level, GameProfile name, CallbackInfo ci) {
        FakePlayerHolder.holdPlayer((FakePlayer) (Object) this);
    }

}
