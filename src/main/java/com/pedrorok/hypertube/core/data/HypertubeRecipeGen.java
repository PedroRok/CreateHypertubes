package com.pedrorok.hypertube.core.data;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * @author Rok, Pedro Lucas nmm. Created on 05/06/2025
 * @project Create Hypertube
 */
public class HypertubeRecipeGen extends RecipeProvider {

    public HypertubeRecipeGen(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.HYPERTUBE.get(), 16)
                .pattern("BGB")
                .pattern("G G")
                .pattern("BGB")
                .define('G', Tags.Items.GLASS_PANES)
                .define('B', AllItems.BRASS_SHEET)
                .unlockedBy("has_brass_sheet", has(AllItems.BRASS_SHEET))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.HYPERTUBE_ENTRANCE.get())
                .pattern(" K ")
                .pattern(" C ")
                .pattern(" G ")
                .define('K', Items.DRIED_KELP)
                .define('C', AllBlocks.SMART_CHUTE)
                .define('G', AllBlocks.COGWHEEL)
                .unlockedBy("has_brass_sheet", has(AllItems.BRASS_SHEET))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.HYPER_ACCELERATOR.get(), 2)
                .pattern(" P ")
                .pattern("CEC")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('E', ModBlocks.HYPERTUBE_ENTRANCE.get())
                .define('C', AllBlocks.COGWHEEL)
                .unlockedBy("has_precision_mechanism", has(AllItems.PRECISION_MECHANISM))
                .save(consumer, ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "hyper_accelerator_small_cogwheel"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.HYPER_ACCELERATOR.get(), 2)
                .pattern(" P ")
                .pattern(" E ")
                .pattern(" C ")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('E', ModBlocks.HYPERTUBE_ENTRANCE.get())
                .define('C', AllBlocks.LARGE_COGWHEEL)
                .unlockedBy("has_precision_mechanism", has(AllItems.PRECISION_MECHANISM))
                .save(consumer, ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "hyper_accelerator_large_cogwheel"));
    }
}