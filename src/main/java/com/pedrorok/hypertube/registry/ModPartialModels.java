package com.pedrorok.hypertube.registry;

import com.jozufozu.flywheel.core.PartialModel;
import com.pedrorok.hypertube.HypertubeMod;
import com.simibubi.create.Create;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/06/2025
 * @project Create Hypertube
 */
public class ModPartialModels {

    public static PartialModel COGWHEEL_HOLE = block("hypertube_entrance/cogwheel_hole");

    private static PartialModel block(String path) {
        return new PartialModel(new ResourceLocation(HypertubeMod.MOD_ID, "block/" + path));
    }

    public static void init() {

    }
}
