package com.matyrobbrt.sectionprotection.mixin.lectern;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.matyrobbrt.sectionprotection.util.FakePlayerHolder;
import com.matyrobbrt.sectionprotection.util.Constants;
import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.LecternExtension;
import com.matyrobbrt.sectionprotection.util.Utils;
import com.matyrobbrt.sectionprotection.world.Banners;
import com.matyrobbrt.sectionprotection.world.ClaimedChunks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Mixin(LecternBlockEntity.class)
public abstract class MixinLecternBE extends BlockEntity implements LecternExtension {
    private static final String sp$REQUEST_UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String sp$FAKE_PLAYER_SUFFIX = "-FP";

    public MixinLecternBE(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    @Shadow
    ItemStack book;

    @Shadow
    protected abstract ItemStack resolveBook(ItemStack pStack, @Nullable Player pPlayer);

    private boolean isProtectionLectern;

    @Override
    public boolean isProtectionLectern() {
        return isProtectionLectern;
    }

    @Override
    public void setProtectionLectern(boolean isProtectionBanner) {
        this.isProtectionLectern = isProtectionBanner;
    }

    @Inject(
        method = "saveAdditional(Lnet/minecraft/nbt/CompoundTag;)V",
        at = @At("TAIL")
    )
    private void sectionprotection$saveAdditional(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean(Constants.PROTECTION_LECTERN, isProtectionLectern);
    }

    @Inject(
        method = "load(Lnet/minecraft/nbt/CompoundTag;)V",
        at = @At("TAIL")
    )
    private void sectionprotection$load(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(Constants.PROTECTION_LECTERN)) {
            isProtectionLectern = tag.getBoolean(Constants.PROTECTION_LECTERN);
        }
    }

    @SuppressWarnings("ALL")
    @Inject(method = "setBook(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"))
    private void sectionprotection$tryLoadTeam(ItemStack pStack, Player pPlayer, @Nullable CallbackInfo ci) {
        if (pPlayer == null || level == null || level.isClientSide() || !isProtectionLectern || pStack.isEmpty())
            return;

        final var manager = ClaimedChunks.get(level);
        final var chunk = new ChunkPos(this.worldPosition);
        if (!manager.isOwned(chunk)) {
            return;
        }

        if (!WritableBookItem.makeSureTagIsValid(pStack.getTag()))
            return;

        final var list = pStack.getOrCreateTag().getList(WrittenBookItem.TAG_PAGES, 8)
                .stream()
                .map(Tag::getAsString)
                .map(l -> {
                    try {
                        final var json = Constants.GSON.fromJson(l, JsonObject.class);
                        final var comp = Component.Serializer.fromJson(json);
                        var str = comp.getContents();
                        for (final var s : comp.getSiblings()) {
                            str = str + s + "\n";
                        }
                        return str;
                    } catch (Exception ignored) {
                        return l;
                    }
                })
                .<String>mapMulti((s, cons) -> {
                    for (final var sub : s.split("\n")) {
                        if (!sub.isBlank()) {
                            cons.accept(sub);
                        }
                    }
                })
                .toList();
        final List<UUID> players = new ArrayList<>();
        for (var name : list) {
            name = name.toUpperCase(Locale.ROOT);
            final Optional<UUID> profile;
            if (name.endsWith(sp$FAKE_PLAYER_SUFFIX)) {
                profile = Optional.ofNullable(FakePlayerHolder.getFakePlayer(name.substring(0, name.length() - sp$FAKE_PLAYER_SUFFIX.length())))
                        .map(FakePlayer::getUUID);
            } else {
                final String finalName = name;
                profile = level.getServer().getProfileCache().get(name)
                        .map(GameProfile::getId)
                        .or(() -> {
                            try {
                                final var url = new URL(sp$REQUEST_UUID_URL + finalName);
                                try (final var reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                                    final var json = Constants.GSON.fromJson(reader, JsonObject.class);
                                    return Optional.of(UUID.fromString(json.get("id").getAsString()));
                                }
                            } catch (Exception e) {
                                SectionProtection.LOGGER.error("Error trying to resolve player UUID for name {}: ", finalName, e);
                            }
                            return Optional.empty();
                        });
            }
            profile.ifPresent(players::add);
        }

        final var banners = Banners.get(level.getServer());
        final var team = banners.getMembers(manager.getOwner(chunk));
        if (!players.isEmpty()) { // If empty, they want a list of members already in the team
            if (team == null) {
                banners.createTeam(manager.getOwner(chunk), pPlayer.getUUID());
                players.remove(pPlayer.getUUID());
                banners.getMembers(manager.getOwner(chunk)).addAll(players);
            } else {
                if (!team.contains(pPlayer.getUUID())) {
                    return; // Don't do stuff on behlaf of another team
                }

                if (!players.contains(pPlayer.getUUID())) {
                    players.add(pPlayer.getUUID());
                }
                if (team.size() > 0 && !players.contains(team.get(0))) {
                    if (players.size() < 1) {
                        players.add(team.get(0));
                    } else {
                        players.add(0, team.get(0)); // Make sure owner is there
                    }
                }
                team.clear();
                team.addAll(players);
            }
            banners.setDirty();
        }

        // Now recreate the book
        final var newTeam = banners.getMembers(manager.getOwner(chunk));
        final var newBook = new ItemStack(pStack.getItem());
        final var newList = new ListTag();
        Lists.partition(newTeam, 6).forEach(sub -> {
            var str = "";
            for (int i = 0; i < sub.size(); i++) {
                final var uuid = sub.get(i);
                var name = Utils.getPlayerName(level.getServer(), uuid);
                if (name.isPresent()) {
                    str = str + name.get();
                    if (i != sub.size() - 1) {
                        str = str + "\n";
                    }
                } else {
                    // Try a fake player
                    final var fakePlayer = FakePlayerHolder.getFakePlayer(uuid);
                    if (fakePlayer != null) {
                        str = str + fakePlayer.getGameProfile().getName() + sp$FAKE_PLAYER_SUFFIX;
                        if (i != sub.size() - 1) {
                            str = str + "\n";
                        }
                    }
                }
            }
            newList.add(StringTag.valueOf(str));
        });
        newBook.getOrCreateTag().put("pages", newList);

        this.book = resolveBook(newBook, pPlayer);
    }

}
