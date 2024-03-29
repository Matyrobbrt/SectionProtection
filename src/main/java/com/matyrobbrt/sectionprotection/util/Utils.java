package com.matyrobbrt.sectionprotection.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import com.matyrobbrt.sectionprotection.util.function.ExceptionSupplier;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Utils {

    private static final Cache<UUID, String> NAME_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.HOURS)
            .build();

    // TODO maybe also make a request to mojang's servers?
    public static Optional<String> getOwnerName(@Nullable MinecraftServer server, List<UUID> team) {
        if (team == null || team.size() < 1) {
            return Optional.empty();
        }
        return getPlayerName(server, team.get(0));
    }

    public static Optional<String> getPlayerName(@Nullable MinecraftServer server, @Nonnull UUID id) {
        if (server != null) {
            final var maybe = server.getProfileCache().get(id).map(GameProfile::getName);
            if (maybe.isPresent()) {
                return maybe;
            }
        }
        final var cached = NAME_CACHE.getIfPresent(id);
        if (cached == null) {
            // Now try a query to mojang's servers
            final var mojang = requestPlayerName(id);
            if (mojang.isPresent()) {
                NAME_CACHE.put(id, mojang.get());
                return mojang;
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(cached);
    }

    public static Optional<String> requestPlayerName(UUID uuid) {
        try {
            final var url = new URL(Constants.REQUEST_NAME_URL + uuid.toString().replace("-", ""));
            try (final var reader = new InputStreamReader(url.openStream())) {
                final var json = Constants.GSON.fromJson(reader, JsonObject.class);
                return Optional.of(json.get("name").getAsString());
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    public static void setLore(CompoundTag tag, Component... components) {
        final var displayTag = new CompoundTag();
        final var lore = new ListTag();
        for (final var comp : components) {
            lore.add(StringTag.valueOf(Component.Serializer.toJson(comp)));
        }
        displayTag.put("Lore", lore);
        tag.put("display", displayTag);
    }

    public static void setLore(ItemStack stack, Component... components) {
        setLore(stack.getOrCreateTag(), components);
    }

    // TODO better validation
    public static Collection<ChunkPos> chunkPosFromString(String str) {
        if (str.startsWith("s")) {
            final var posIndex = str.indexOf(':');
            final var area = Integer.parseInt(str.substring(1, posIndex)) / 2;
            final var tgtStr = str.substring(posIndex + 1);
            final var spl = tgtStr.split(",");
            final var startPos = new ChunkPos(Integer.parseInt(spl[0]), Integer.parseInt(spl[1]));

            final Set<ChunkPos> pos = new HashSet<>();
            pos.add(startPos);
            for (int x = -area; x <= area; x++) {
                for (int z = -area; z <= area; z++) {
                    final var relativeX = startPos.x + x;
                    final var relativeZ = startPos.z + z;
                    pos.add(new ChunkPos(relativeX, relativeZ));
                    pos.add(new ChunkPos(relativeZ, relativeX));
                }
            }
            return pos;
        }
        final var spl = str.split(",");
        if (spl.length != 2) {
            return List.of(ChunkPos.ZERO);
        }
        return List.of(new ChunkPos(Integer.parseInt(spl[0]), Integer.parseInt(spl[1])));
    }

    public static <T> T getOrNull(ExceptionSupplier<? extends T> sup) {
        try {
            return sup.get();
        } catch (Throwable t) {
            return null;
        }
    }
}
