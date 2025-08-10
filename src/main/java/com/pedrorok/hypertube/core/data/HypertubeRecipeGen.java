package com.pedrorok.hypertube.core.data;

import com.pedrorok.hypertube.registry.ModBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

/**
 * @author Rok, Pedro Lucas nmm. Created on 05/06/2025
 * @project Create Hypertube
 */
public class HypertubeRecipeGen extends RecipeProvider {

    public HypertubeRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput consumer) {
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

        // TODO: add recipe for Hypertube accelerator
    }
}