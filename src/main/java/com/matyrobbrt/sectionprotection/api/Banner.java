package com.matyrobbrt.sectionprotection.api;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.matyrobbrt.sectionprotection.api.Banner.Data;

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

    public ListTag serialize() {
        final var tag = new ListTag();
        for (int i = 0; i < data.size(); ++i) {
            tag.add(data.get(i).serialize());
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
