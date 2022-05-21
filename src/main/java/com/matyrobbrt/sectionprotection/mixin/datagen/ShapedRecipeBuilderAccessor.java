package com.matyrobbrt.sectionprotection.mixin.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@Mixin(ShapedRecipeBuilder.class)
public interface ShapedRecipeBuilderAccessor {

    @Accessor
    Item getResult();

    @Accessor
    int getCount();

    @Accessor
    List<String> getRows();

    @Accessor
    Advancement.Builder getAdvancement();

    @Accessor
    Map<Character, Ingredient> getKey();

    @Nullable
    @Accessor
    String getGroup();
}
