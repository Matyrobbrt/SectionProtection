package com.matyrobbrt.sectionprotection;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = SectionProtection.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SPDatagen {

    @SubscribeEvent
    static void onDatagen(final GatherDataEvent event) {
        final var gen = event.getGenerator();
        gen.addProvider(new RecipeProvider(gen));
    }

    static class RecipeProvider extends net.minecraft.data.recipes.RecipeProvider {

        public RecipeProvider(DataGenerator pGenerator) {
            super(pGenerator);
        }

        @Override
        @SuppressWarnings("deprecation")
        protected void buildCraftingRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
            for (final var dye : DyeColor.values()) {
                final var banner = BannerBlock.byColor(dye);
                ShapelessRecipeBuilder.shapeless(banner)
                    .group("banners")
                    .unlockedBy("has_dye", has(dye.getTag()))
                    .requires(ItemTags.BANNERS)
                    .requires(dye.getTag())
                    .save(consumer, new ResourceLocation(SectionProtection.MOD_ID, "banner_recolour/" + dye.getName()));

                ShapedRecipeBuilder.shaped(banner, 2)
                    .group("sp_banners")
                    .unlockedBy("has_dye", has(dye.getTag()))
                    .pattern("CCC")
                    .pattern("CCC")
                    .pattern(" S ")
                    .define('C', Registry.ITEM.get(new ResourceLocation(dye.getName() + "_carpet")))
                    .define('S', Tags.Items.RODS_WOODEN)
                    .save(consumer, new ResourceLocation(SectionProtection.MOD_ID, "easier_banners/" + dye.getName()));

                ShapedRecipeBuilder.shaped(banner, 2)
                    .group("sp_banners")
                    .unlockedBy("has_dye", has(dye.getTag()))
                    .pattern("CCC")
                    .pattern("CCC")
                    .pattern("DSD")
                    .define('C', ItemTags.CARPETS)
                    .define('S', Tags.Items.RODS_WOODEN)
                    .define('D', dye.getTag())
                    .save(consumer, new ResourceLocation(SectionProtection.MOD_ID, "easier_banners/" + dye.getName() + "_wildcard"));
            }
        }
    }
}
