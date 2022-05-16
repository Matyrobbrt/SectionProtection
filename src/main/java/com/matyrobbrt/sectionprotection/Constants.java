package com.matyrobbrt.sectionprotection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.function.UnaryOperator;

public class Constants {

    public static final String PROTECTION_BANNER = SectionProtection.MOD_ID + ":protection_banner";
    public static final String PROTECTION_LECTERN = SectionProtection.MOD_ID + ":protection_lectern";

    public static final Gson GSON = new GsonBuilder().setLenient().create();

    public static final String REQUEST_NAME_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    public static final int PLAYER_NAME_COLOUR = 0x009B00;
    public static final UnaryOperator<Style> WITH_PLAYER_NAME = s -> s.withColor(PLAYER_NAME_COLOUR);
}
