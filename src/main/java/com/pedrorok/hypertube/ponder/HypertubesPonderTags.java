package com.pedrorok.hypertube.ponder;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModItems;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

/**
 * @author Rok, Pedro Lucas nmm. 04/02/2026
 * @project Create Hypertube
 */
public class HypertubesPonderTags {

    public static final ResourceLocation

            HYPERTUBE_SYSTEMS = loc("hypertube_systems");

    private static ResourceLocation loc(String id) {
        return HypertubeMod.of(id);
    }

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {

        PonderTagRegistrationHelper<RegistryEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(
                RegisteredObjectsHelper::getKeyOrThrow);

        helper.registerTag(HYPERTUBE_SYSTEMS)
                .addToIndex()
                .item(ModBlocks.HYPERTUBE.get(), true, false)
                .title("Hypertube Systems")
                .description("Blocks and items used in Hypertube transportation systems.")
                .register();

        HELPER.addToTag(HYPERTUBE_SYSTEMS)
                .add(ModBlocks.HYPERTUBE)
                .add(ModBlocks.HYPERTUBE_ENTRANCE)
                .add(ModBlocks.HYPER_ACCELERATOR)
                .add(ModItems.REDSTONE_DETECTOR)
                .add(ModItems.TUBE_SCANNER)
        ;
    }

}
