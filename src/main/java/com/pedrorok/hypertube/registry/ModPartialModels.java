package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.simibubi.create.Create;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/06/2025
 * @project Create Hypertube
 */
public class ModPartialModels {

    public static PartialModel COGWHEEL_HOLE = block("cogwheel_hole");

    private static PartialModel block(String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "block/" + path));
    }

    public static void init() {

    }
}
