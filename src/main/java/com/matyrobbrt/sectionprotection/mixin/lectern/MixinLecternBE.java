package com.matyrobbrt.sectionprotection.mixin.lectern;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.matyrobbrt.sectionprotection.Constants;
import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.api.ClaimedChunk;
import com.matyrobbrt.sectionprotection.api.LecternExtension;
import com.matyrobbrt.sectionprotection.world.Banners;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(LecternBlockEntity.class)
public abstract class MixinLecternBE extends BlockEntity implements LecternExtension {
    private static final String sp$REQUEST_UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String sp$REQUEST_NAME_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

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
        if (pPlayer == null || level == null || level.isClientSide() || !isProtectionLectern)
            return;

        final var cap = level.getChunkAt(worldPosition).getCapability(ClaimedChunk.CAPABILITY).orElse(null);
        if (cap == null || cap.getOwningBanner() == null) {
            return;
        }

        if (!WrittenBookItem.makeSureTagIsValid(pStack.getTag()))
            return;

        final var list = pStack.getOrCreateTag().getList("pages", 8)
                .stream()
                .map(t -> t.getAsString())
                .<String>mapMulti((s, cons) -> {
                    for (final var sub : s.split("\\n")) {
                        cons.accept(sub);
                    }
                })
                .toList();
        final List<UUID> players = new ArrayList<>();
        for (final var l : list) {
            String name;
            try {
                name = Component.Serializer.fromJsonLenient(l).getContents();
            } catch (Exception ignored) {
                name = l;
            }
            final var finalName = name;
            final var profile = level.getServer().getProfileCache().get(name)
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
            profile.ifPresent(players::add);
        }

        final var banners = Banners.get(level.getServer());
        final var team = banners.getMembers(cap.getOwningBanner());
        if (team == null) {
            banners.createTeam(cap.getOwningBanner(), pPlayer.getUUID());
            if (players.contains(pPlayer.getUUID())) {
                players.remove(pPlayer.getUUID());
            }
            banners.getMembers(cap.getOwningBanner()).addAll(players);
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

        // Now recreate the book
        final var newBook = new ItemStack(Items.WRITTEN_BOOK);
        final var newList = new ListTag();
        Lists.partition(players, 6).forEach(sub -> {
            var str = "";
            for (int i = 0; i < sub.size(); i++) {
                final var name = getName(sub.get(i));
                if (!name.isBlank()) {
                    str = str + name;
                    if (i != sub.size() - 1) {
                        str = str + "\\n";
                    }
                }
            }
            newList.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(str))));
        });
        newBook.getOrCreateTag().put("pages", newList);

        this.book = resolveBook(newBook, pPlayer);
    }

    // TODO refactor
    private static String getName(UUID uuid) {
        String output = callURL(sp$REQUEST_NAME_URL + uuid.toString().replaceAll("-", ""));
        StringBuilder result = new StringBuilder();
        readName(output, result);
        return result.toString();
    }

    private static String callURL(String URL) {
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn;
        InputStreamReader in = null;
        try {
            URL url = new URL(URL);
            urlConn = url.openConnection();

            if (urlConn != null)
                urlConn.setReadTimeout(60 * 1000);

            if (urlConn != null && urlConn.getInputStream() != null) {
                in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);
                int cp;
                while ((cp = bufferedReader.read()) != -1) {
                    sb.append((char) cp);
                }
                bufferedReader.close();
            }

            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
            SectionProtection.LOGGER.error("Exception trying to get player name: ", e);
        }
        return sb.toString();
    }

    private static void readName(String toRead, StringBuilder result) {
        int i = 49;
        while (i < 200) {
            if (!String.valueOf(toRead.charAt(i)).equalsIgnoreCase("\"")) {
                result.append(toRead.charAt(i));
            } else {
                break;
            }
            i++;
        }
    }

}
