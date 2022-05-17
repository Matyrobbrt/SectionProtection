package com.matyrobbrt.sectionprotection.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matyrobbrt.sectionprotection.SectionProtection;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.UnaryOperator;

public class Constants {

    public static final String PROTECTION_BANNER = SectionProtection.MOD_ID + ":protection_banner";
    public static final String PROTECTION_LECTERN = SectionProtection.MOD_ID + ":protection_lectern";
    public static final String SP_GUIDE_TAG = SectionProtection.MOD_ID + ":guide";

    public static final Gson GSON = new GsonBuilder().setLenient().create();

    public static final String REQUEST_NAME_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public static final int PLAYER_NAME_COLOUR = 0x009B00;
    public static final UnaryOperator<Style> WITH_PLAYER_NAME = s -> s.withColor(PLAYER_NAME_COLOUR);

    // TODO try making the book better
    public static final ItemStack SP_BOOK = BookBuilder.builder()
        .author("Matyrobbrt")
        .title("SectionProtection Guide")
        .addPage("""
                  How to use the mod
                The usage is very simple, and vanilla-esque. Each team is represented by a banner pattern.
                A banner can be made a "Protecting" one by right clicking it with an item from the `sectionprotection:conversion_item` tag (Netherite Ingot only by default).""")
        .addPage("A Protecting Banner can be placed in a chunk, making that chunk protected, and allowing action inside it to be done only by members of the banner's team.")

        .addPage("""
                   How do teams work
                A team represents a set of players that can interact with chunks claimed by a protecting banner with a specific pattern.
                When a Protecting Banner with a new pattern has been created, a team for that banner will be created as well, which, at that point, only""")
        .addPage(" contains the player that created the banner.")
        .addPage("In order to add players to a team, a \"Protecting\" Lectern needs to be made, which can be obtained in the same way Protecting Banners are obtained (right clicking with a conversion item). A Protecting Lectern can be placed in a chunk claimed with a Protecting Banner in")
        .addPage("order to configure the members of that banner's team. Placing a book in the lectern will add all players with their names in the book to the team (§lthis process is overriding the old members§r). Placing an empty book will make the lectern add all current members to the book.")
        .extraNBT(c -> c.putBoolean(SP_GUIDE_TAG, true))
        .build(new ItemStack(Items.WRITTEN_BOOK));
}
