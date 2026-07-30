package com.pedrorok.hypertube.core.data;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.data.recipe.MechanicalCraftingRecipeBuilder;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HYPERTUBE_FUNNEL.get())
                .pattern(" B ")
                .pattern(" H ")
                .pattern(" K ")
                .define('B', AllItems.BRASS_SHEET)
                .define('H', ModBlocks.HYPERTUBE.get())
                .define('K', Items.DRIED_KELP)
                .unlockedBy("has_brass_sheet", has(AllItems.BRASS_SHEET))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.HYPERTUBE_ENTRANCE.get())
                .pattern(" F ")
                .pattern(" G ")
                .pattern(" C ")
                .define('F', ModItems.HYPERTUBE_FUNNEL)
                .define('G', AllBlocks.COGWHEEL)
                .define('C', AllBlocks.SMART_CHUTE)
                .unlockedBy("has_brass_sheet", has(AllItems.BRASS_SHEET))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.HYPER_ACCELERATOR.get())
                .pattern(" F ")
                .pattern("CPC")
                .pattern(" F ")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('F', ModItems.HYPERTUBE_FUNNEL.get())
                .define('C', AllBlocks.COGWHEEL)
                .unlockedBy("has_precision_mechanism", has(AllItems.PRECISION_MECHANISM))
                .save(consumer, HypertubeMod.of("hyper_accelerator_small_cogwheel"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REDSTONE_DETECTOR.get())
                .pattern("ACA")
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

        //MechanicalCraftingRecipeBuilder.shapedRecipe(ModBlocks.HYPER_JUNCTION.get())
        //        .key('B', AllItems.BRASS_SHEET)
        //        .key('T', AllItems.)
        //        .key('F', ModItems.HYPERTUBE_FUNNEL)
        //        .key('H', ModBlocks.HYPERTUBE)
        //        .patternLine("BTB")
        //        .patternLine("FHF")
        //        .patternLine(" F ")
        //        .build(consumer);
    }
}