package com.matyrobbrt.sectionprotection.recipe;

import com.google.gson.JsonObject;
import com.matyrobbrt.sectionprotection.SectionProtection;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public record RecipeEnabledCondition(String type) implements ICondition {

    public static final Map<String, BooleanSupplier> TYPES = new HashMap<>();
    public static final ResourceLocation ID = new ResourceLocation(SectionProtection.MOD_ID, "enabled");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        // TODO make configs work again, with json configs because server configs are loaded too late
        return true;
    }

    public static final class Serializer implements IConditionSerializer<RecipeEnabledCondition> {

        @Override
        public void write(JsonObject json, RecipeEnabledCondition value) {
            json.addProperty("recipe_type", value.type());
        }

        @Override
        public RecipeEnabledCondition read(JsonObject json) {
            return new RecipeEnabledCondition(json.get("recipe_type").getAsString());
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
