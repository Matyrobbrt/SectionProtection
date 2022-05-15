package com.matyrobbrt.sectionprotection.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

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
