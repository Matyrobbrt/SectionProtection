package com.matyrobbrt.sectionprotection.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.matyrobbrt.sectionprotection.SectionProtection;
import com.matyrobbrt.sectionprotection.ServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static net.minecraft.ChatFormatting.*;

public class Constants {

    public static final String PROTECTION_BANNER = SectionProtection.MOD_ID + ":protection_banner";
    public static final String PROTECTION_LECTERN = SectionProtection.MOD_ID + ":protection_lectern";
    public static final String SP_GUIDE_TAG = SectionProtection.MOD_ID + ":guide";

    public static final Gson GSON = new GsonBuilder().setLenient().create();

    public static final String REQUEST_NAME_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public static final int PLAYER_NAME_COLOUR = 0x009B00;
    public static final UnaryOperator<Style> WITH_PLAYER_NAME = s -> s.withColor(PLAYER_NAME_COLOUR);

    public static final Supplier<ItemStack> SP_BOOK = () -> BookBuilder.builder()
        .author("Matyrobbrt")
        .title("SectionProtection Guide")
        .addPage(new TextComponent(" ")
                .append(t("Section Protection").withStyle(BOLD).withStyle(UNDERLINE))
                .append(t("\n\nA page by page Guide to using Section Protection, your Banner-based claiming solution.\n \u0020 \u0020 \u0020 \u0020 \n \u0020 \u0020 \u0020 \u0020 \u0020 By\n \u0020 \u0020 \u0020Matyrobbrt")))
        .addPage(component().append(styled("1. Getting Started:", UNDERLINE))
                .append(t("\nTo get started, all you need is a Banner.\nIf you want to play with multiple people on the same claim, you also need a Lectern.\nTo convert these 2 Blocks into the correct type, right click them with an item tagged as:\n"))
                .append(t("\"sectionprotection:\nconversion_item\"").withStyle(UNDERLINE).withStyle(s -> s.withColor(0x71DC83).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("By default, it's a Netherite Ingot per Banner and Lectern."))))))
        .addPage(component().append(styled("2. Setting up a camp:", UNDERLINE))
                .append(t("\nPlace the Banner and the Lectern down and click the conversion item on them.\nCongratulations, you have created a team and claimed your first Chunk.")))
        .addPage(component().append(styled("3. Teams:", UNDERLINE))
                .append("\nTo add members to the Team, you need a ")
                .append(t("Book and Quill.").withStyle(UNDERLINE).withStyle(s -> s.withColor(0x71DC83).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(Items.WRITABLE_BOOK.getDefaultInstance())))))
                .append("\nAdd the players names to the Book and place it inside the Lectern. ")
                .append(t("Do ").withStyle(RED).append(t("not").withStyle(BOLD).withStyle(UNDERLINE).append(t(" sign the book.").withStyle(RED))))
                .append("\nThey should be able to help you build now.\nFurther information can be gotten at the last page."))
        .addPage(component().append(styled("4. FakePlayers:", UNDERLINE))
                .append("\nIn order to add a fake player to your team, you first need to know it's name. Run \"/sectionprotection fake_players list\" to get a list of all fake players in the server.")
                .append("In the book, a fake player can be whitelisted using its name and the \"-FP\" suffix."))
        .addPage(component().append(styled("5. Extending the Claim:", UNDERLINE))
                .append("\nTo extend your claim, simply copy the first Banner you used.\nThis is done by crafting the Banner with the Pattern together with a blank Banner of the same base color.\nYou will not lose the converted Banner during that process.\n \u0020 \u0020 \u0020 \u0020 \u0020-->"))
            .addPage("Now place the first Banner back and spread the newly made ones out and convert them.\nRemember, each Banner protects a radius of " + ServerConfig.CLAIM_RADIUS.get() + " blocks around itself.\nThe Lectern can stay in any of the claimed Chunks.")
        .addPage(component().append(styled("6. Who's on your team?", UNDERLINE))
                .append("\nYou can find out by placing an empty ")
                .append(t("Book and Quill").withStyle(s -> s.withColor(UNDERLINE).withColor(0x71DC83).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(Items.WRITABLE_BOOK.getDefaultInstance())))))
                .append(" inside the converted Lectern.\nIt will be filled with the names of all the current team members."))
        .addPage(component().append(styled("7. Commands:", UNDERLINE))
                .append("\n\n/sectionprotection\n\nIt's self explanatory from there."))
        .addPage(t("The Page About Patterns:\n\nEach ")
                .append(t("Pattern").withStyle(DARK_AQUA))
                .append(" represents a unique Team. There are about\n800 quadrillion combinations craftable."))
        .extraNBT(c -> {
            c.putBoolean(SP_GUIDE_TAG, true);
            Utils.setLore(c, new TextComponent("This book contains useful information on how to claim chunks."));
        })
        .build(new ItemStack(Items.WRITTEN_BOOK));

    private static TextComponent t(String str) {
        return new TextComponent(str);
    }

    private static MutableComponent styled(String str, ChatFormatting... formatting) {
        return new TextComponent(str).withStyle(formatting);
    }

    private static MutableComponent component() {
        return new TextComponent("");
    }
}
