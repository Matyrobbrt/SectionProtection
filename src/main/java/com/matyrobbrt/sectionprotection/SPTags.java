package com.matyrobbrt.sectionprotection;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static net.minecraft.core.Registry.*;

@SuppressWarnings("SameParameterValue")
public class SPTags {

    public static final TagKey<Item> IS_CONVERSION_ITEM = create(ITEM_REGISTRY, "conversion_item");

    private static <K> TagKey<K> create(ResourceKey<? extends Registry<K>> key, String name) {
        return TagKey.create(key, new ResourceLocation(SectionProtection.MOD_ID, name));
    }
}
