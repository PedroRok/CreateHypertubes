package com.pedrorok.hypertube.ponder;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModItems;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderTag;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Rok, Pedro Lucas nmm. 04/02/2026
 * @project Create Hypertube
 */
public class HypertubesPonderTags {

    public static final PonderTag HYPERTUBE_SYSTEMS = create("hypertube_systems")
            .item(ModBlocks.HYPERTUBE)
            .defaultLang("Hypertube Systems", "Blocks and items used in Hypertube transportation systems.")
            .addToIndex();

    private static ResourceLocation loc(String id) {
        return new ResourceLocation(HypertubeMod.MOD_ID, id);
    }

    public static void register() {

        PonderRegistry.TAGS.forTag(HYPERTUBE_SYSTEMS)
                .add(ModBlocks.HYPERTUBE)
                .add(ModBlocks.HYPERTUBE_ENTRANCE)
                .add(ModBlocks.HYPER_ACCELERATOR)
                .add(ModItems.REDSTONE_DETECTOR)
                .add(ModItems.TUBE_SCANNER);
    }


    private static PonderTag create(String id) {
        return new PonderTag(loc(id));
    }

}
