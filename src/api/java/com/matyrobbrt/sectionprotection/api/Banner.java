package com.matyrobbrt.sectionprotection.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;

import java.util.ArrayList;
import java.util.List;

public record Banner(List<Data> data) {

    public Banner(ListTag nbt) {
        this(Util.make(() -> {
            final var data = ImmutableList.<Data>builder();
            for (int i = 0; i < nbt.size(); ++i) {
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
            if (nbt == null) {
                return data.build();
            }
            for (int i = 0; i < nbt.size(); ++i) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Banner banner = (Banner) o;
        return Iterables.elementsEqual(data, banner.data);
    }

    @Override
    public int hashCode() {
        return hash(data);
    }

    public static int hash(List<?> list) {
        int hashCode = 1;
        for (final Object e : list)
            hashCode = 31* hashCode + (e==null ? 0 : e.hashCode());
        return hashCode;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(data, (bf, d) -> d.encode(bf));
    }

    public static Banner decode(FriendlyByteBuf buf) {
        return new Banner(buf.readCollection(ArrayList::new, Data::decode));
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

        public void encode(FriendlyByteBuf buf) {
            buf.writeInt(color.getId());
            buf.writeUtf(pattern.getHashname(), 5);
        }

        public static Data decode(FriendlyByteBuf buf) {
            return new Data(buf.readInt(), buf.readUtf(5));
        }
    }

    public static class Builder {
        private final List<Data> data = new ArrayList<>();

        public Builder add(BannerPattern pattern, DyeColor colour) {
            this.data.add(new Data(colour, pattern));
            return this;
        }

        public Builder add(String pattern, int colour) {
            return add(BannerPattern.byHash(pattern), DyeColor.byId(colour));
        }

        public Banner build() {
            return new Banner(List.copyOf(data));
        }
    }
}
