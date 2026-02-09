package com.pedrorok.hypertube.core.data;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.HYPER_ACCELERATOR.get(), 2)
                .pattern(" P ")
                .pattern("CEC")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('E', ModBlocks.HYPERTUBE_ENTRANCE.get())
                .define('C', AllBlocks.COGWHEEL)
                .unlockedBy("has_precision_mechanism", has(AllItems.PRECISION_MECHANISM))
                .save(consumer, HypertubeMod.of("hyper_accelerator_small_cogwheel"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.HYPER_ACCELERATOR.get(), 2)
                .pattern(" P ")
                .pattern(" E ")
                .pattern(" C ")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('E', ModBlocks.HYPERTUBE_ENTRANCE.get())
                .define('C', AllBlocks.LARGE_COGWHEEL)
                .unlockedBy("has_precision_mechanism", has(AllItems.PRECISION_MECHANISM))
                .save(consumer, HypertubeMod.of("hyper_accelerator_large_cogwheel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REDSTONE_DETECTOR.get())
                .pattern( "ACA")
                .pattern("AHA")
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('C', Items.COMPARATOR)
                .define('H', AllItems.BRASS_HAND)
                .unlockedBy("has_hypertube_entrance", has(ModBlocks.HYPERTUBE_ENTRANCE))
                .save(consumer, HypertubeMod.of("redstone_detector_tube_attachment"));

        new SequencedAssemblyRecipeBuilder(HypertubeMod.of("tube_scanner"))
                .transitionTo(ModItems.TUBE_SCANNER_UNFINISHED)
                .addOutput(ModItems.TUBE_SCANNER, 100)
                .addStep(DeployerApplicationRecipe::new, rb -> rb.require(AllItems.BRASS_SHEET))
                .addStep(DeployerApplicationRecipe::new, rb -> rb.require(AllItems.ELECTRON_TUBE))
                .addStep(DeployerApplicationRecipe::new, rb -> rb.require(AllItems.BRASS_SHEET))
                .require(ModItems.REDSTONE_DETECTOR)
                .loops(1)
                .build(consumer);

    }
}