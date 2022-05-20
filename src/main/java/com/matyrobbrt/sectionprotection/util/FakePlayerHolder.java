package com.matyrobbrt.sectionprotection.util;

import com.mojang.authlib.GameProfile;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FakePlayerHolder {

    public static final Map<GameProfile, WeakReference<FakePlayer>> FAKE_PLAYERS = new HashMap<>();

    public static void holdPlayer(FakePlayer player) {
        FAKE_PLAYERS.put(player.getGameProfile(), new WeakReference<>(player));
    }

    @Nullable
    public static FakePlayer getFakePlayer(GameProfile profile) {
        final var ref = FAKE_PLAYERS.get(profile);
        if (ref == null)
            return null;
        final var player = ref.get();
        if (player == null)
            FAKE_PLAYERS.remove(profile); // Reference is lost, no need to keep it anymore
        return player;
    }

    @Nullable
    public static FakePlayer getFakePlayer(String name) {
        final var lower = name.toLowerCase(Locale.ROOT);
        return FAKE_PLAYERS.keySet().stream()
            .filter(prof -> prof.getName().toLowerCase(Locale.ROOT).equals(lower))
            .findFirst()
            .map(FakePlayerHolder::getFakePlayer)
            .orElse(null);
    }

    @Nullable
    public static FakePlayer getFakePlayer(UUID uuid) {
        return FAKE_PLAYERS.keySet().stream()
                .filter(prof -> prof.getId().equals(uuid))
                .findFirst()
                .map(FakePlayerHolder::getFakePlayer)
                .orElse(null);
    }

    public static List<FakePlayer> getAll() {
        return FAKE_PLAYERS.values()
            .stream()
            .map(WeakReference::get)
            .filter(Objects::nonNull) // We lost the reference
            .toList();
    }
}
