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

    public static PartialModel REDSTONE_DETECTOR = block("redstone_detector_tube_attachment");
    public static PartialModel REDSTONE_DETECTOR_ACTIVE = block("redstone_detector_tube_attachment_active");

    public static PartialModel TUBE_SCANNER = block("tube_scanner_attachment");
    public static PartialModel TUBE_SCANNER_ACTIVE = block("tube_scanner_attachment_active");

    private static PartialModel block(String path) {
        return new PartialModel(HypertubeMod.of("block/" + path));
    }

    public static void init() {

    }
}
