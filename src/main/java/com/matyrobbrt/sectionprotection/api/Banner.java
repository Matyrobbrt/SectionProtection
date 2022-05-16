package com.matyrobbrt.sectionprotection.api;

import java.util.List;

import com.google.common.collect.ImmutableList;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;

public record Banner(List<Data> data) {

    public Banner(ListTag nbt) {
        this(Util.make(() -> {
            final var data = ImmutableList.<Data>builder();
            for (int i = 0; i < nbt.size() && i < 6; ++i) {
                CompoundTag compoundtag1 = nbt.getCompound(i);
                data.add(new Data(compoundtag1));
            }
            return data.build();
        }));
    }

    public Banner(ListTag nbt, DyeColor base) {
        this(Util.make(() -> {
            final var data = ImmutableList.<Data>builder();
            data.add(new Data(base, BannerPattern.BASE));
            for (int i = 0; i < nbt.size() && i < 6; ++i) {
                CompoundTag compoundtag1 = nbt.getCompound(i);
                data.add(new Data(compoundtag1));
            }
            return data.build();
        }));
    }

    public static Banner from(List<Pair<BannerPattern, DyeColor>> list) {
        return new Banner(list.stream().map(p -> new Banner.Data(p.getSecond(), p.getFirst())).toList());
    }

    public ListTag serialize() {
        final var tag = new ListTag();
        for (final var datum : data) {
            tag.add(datum.serialize());
        }
        return tag;
    }

    public record Data(DyeColor color, BannerPattern pattern) {

        public Data(int color, String pattern) {
            this(DyeColor.byId(color), BannerPattern.byHash(pattern));
        }

        public Data(CompoundTag nbt) {
            this(nbt.getInt("Color"), nbt.getString("Pattern"));
        }

        public CompoundTag serialize() {
            final var tag = new CompoundTag();
            tag.putInt("Color", color.getId());
            tag.putString("Pattern", pattern.getHashname());
            return tag;
        }
    }
}
