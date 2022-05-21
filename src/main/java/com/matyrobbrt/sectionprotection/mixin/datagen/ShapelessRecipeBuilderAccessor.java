package com.matyrobbrt.sectionprotection.mixin.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(ShapelessRecipeBuilder.class)
public interface ShapelessRecipeBuilderAccessor {

    @Accessor
    Item getResult();

    @Accessor
    int getCount();

    @Accessor
    List<Ingredient> getIngredients();

    @Accessor
    Advancement.Builder getAdvancement();

    @Nullable
    @Accessor
    String getGroup();
}
