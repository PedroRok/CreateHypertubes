package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/06/2025
 * @project Create Hypertube
 */
public class ModPartialModels {

    public static PartialModel COGWHEEL_HOLE = block("hypertube_entrance/cogwheel_hole");

    public static PartialModel REDSTONE_DETECTOR = block("redstone_detector_tube_attachment");
    public static PartialModel REDSTONE_DETECTOR_NO_COG = block("redstone_detector_tube_attachment_no_cog");
    public static PartialModel REDSTONE_DETECTOR_ACTIVE = block("redstone_detector_tube_attachment_active");
    public static PartialModel REDSTONE_DETECTOR_NO_COG_ACTIVE = block("redstone_detector_tube_attachment_no_cog_active");

    public static PartialModel TUBE_SCANNER = block("tube_scanner_attachment");
    public static PartialModel TUBE_SCANNER_NO_COG = block("tube_scanner_attachment_no_cog");
    public static PartialModel TUBE_SCANNER_ACTIVE = block("tube_scanner_attachment_active");
    public static PartialModel TUBE_SCANNER_NO_COG_ACTIVE = block("tube_scanner_attachment_no_cog_active");

    private static PartialModel block(String path) {
        return PartialModel.of(HypertubeMod.of("block/" + path));
    }

    public static void init() {

    }
}
