package com.matyrobbrt.sectionprotection.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.matyrobbrt.sectionprotection.mixin.datagen.ShapelessRecipeBuilderAccessor;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConditionalShapelessRecipeBuilder extends ShapelessRecipeBuilder {
    private final List<ICondition> conditions = new ArrayList<>();

    public static ConditionalShapelessRecipeBuilder shapeless(ItemLike pResult, int pCount) {
        return new ConditionalShapelessRecipeBuilder(pResult, pCount);
    }

    public ConditionalShapelessRecipeBuilder(ItemLike pResult, int pCount) {
        super(pResult, pCount);
    }

    public ConditionalShapelessRecipeBuilder condition(ICondition condition) {
        this.conditions.add(condition);
        return this;
    }

    @Override
    public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId) {
        final var ext = (ShapelessRecipeBuilderAccessor) this;
        ext.getAdvancement().parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pRecipeId)).rewards(AdvancementRewards.Builder.recipe(pRecipeId)).requirements(RequirementsStrategy.OR);
        pFinishedRecipeConsumer.accept(new Result(pRecipeId, ext.getResult(), ext.getCount(), ext.getGroup() == null ? "" : ext.getGroup(), ext.getIngredients(), ext.getAdvancement(), new ResourceLocation(pRecipeId.getNamespace(), "recipes/" + ext.getResult().getItemCategory().getRecipeFolderName() + "/" + pRecipeId.getPath()), conditions));
    }

    public static class Result extends ShapelessRecipeBuilder.Result {
        private final List<ICondition> conditions;
        public Result(ResourceLocation pId, Item pResult, int pCount, String pGroup, List<Ingredient> pIngredients, Advancement.Builder pAdvancement, ResourceLocation pAdvancementId, List<ICondition> conditions) {
            super(pId, pResult, pCount, pGroup, pIngredients, pAdvancement, pAdvancementId);
            this.conditions = conditions;
        }

        @Nonnull
        @Override
        public JsonObject serializeRecipe() {
            final var res = super.serializeRecipe();
            final var cond = new JsonArray();
            conditions.forEach(c -> cond.add(CraftingHelper.serialize(c)));
            res.add("conditions", cond);
            return res;
        }
    }
}
