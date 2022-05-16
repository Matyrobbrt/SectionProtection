package com.matyrobbrt.sectionprotection.util;

import com.google.gson.JsonObject;
import com.matyrobbrt.sectionprotection.Constants;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Utils {

    // TODO maybe also make a request to mojang's servers?
    public static Optional<String> getOwnerName(MinecraftServer server, List<UUID> team) {
        if (team == null || team.size() < 1) {
            return Optional.empty();
        }
        return server.getProfileCache().get(team.get(0)).map(GameProfile::getName);
    }

    public static Optional<String> getPlayerName(@Nullable MinecraftServer server, @Nonnull UUID id) {
        if (server != null) {
            final var maybe = server.getProfileCache().get(id).map(GameProfile::getName);
            if (maybe.isPresent()) {
                return maybe;
            }
        }
        // Now try a query to mojang's servers
        try {
            final var url = new URL(Constants.REQUEST_NAME_URL + id.toString().replace("-", ""));
            try (final var reader = new InputStreamReader(url.openStream())) {
                final var json = Constants.GSON.fromJson(reader, JsonObject.class);
                return Optional.of(json.get("name").getAsString());
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    public static void setLore(ItemStack stack, Component... components) {
        final var displayTag = new CompoundTag();
        final var lore = new ListTag();
        for (final var comp : components) {
            lore.add(StringTag.valueOf(Component.Serializer.toJson(comp)));
        }
        displayTag.put("Lore", lore);
        stack.getOrCreateTag().put("display", displayTag);
    }

}
