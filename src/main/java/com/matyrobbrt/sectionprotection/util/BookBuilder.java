package com.matyrobbrt.sectionprotection.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BookBuilder {

    private String title;
    private String author;
    private final List<Component> pages = new ArrayList<>();
    private Consumer<CompoundTag> extraNbt;

    public BookBuilder addPage(Component page) {
        this.pages.add(page);
        return this;
    }

    public BookBuilder addPage(String page) {
        this.pages.add(Component.literal(page));
        return this;
    }

    public BookBuilder addPage(int index, Component page) {
        this.pages.add(index, page);
        return this;
    }

    public BookBuilder title(String title) {
        this.title = title;
        return this;
    }

    public BookBuilder author(String author) {
        this.author = author;
        return this;
    }

    public BookBuilder extraNBT(Consumer<CompoundTag> extraNbt) {
        this.extraNbt = extraNbt;
        return this;
    }

    public ItemStack build(ItemStack stack) {
        final var copy = stack.copy();
        final var pagesTag = new ListTag();
        pages.forEach(comp -> pagesTag.add(StringTag.valueOf(Component.Serializer.toJson(comp))));
        copy.getOrCreateTag().putBoolean(WrittenBookItem.TAG_RESOLVED, true);
        copy.getOrCreateTag().put(WrittenBookItem.TAG_PAGES, pagesTag);
        if (author != null)
            copy.getOrCreateTag().putString(WrittenBookItem.TAG_AUTHOR, author);
        if (title != null)
            copy.getOrCreateTag().putString(WrittenBookItem.TAG_TITLE, title);
        if (extraNbt != null)
            extraNbt.accept(copy.getOrCreateTag());
        return copy;
    }

    public static BookBuilder builder() {
        return new BookBuilder();
    }
}
