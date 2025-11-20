package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.items.TubeAttachmentItem;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/11/2025
 * @project Create Hypertube
 */
public class ModItems {

    private static final CreateRegistrate REGISTRATE = HypertubeMod.get();

    public static final ItemEntry<TubeAttachmentItem> REDSTONE_DETECTOR = REGISTRATE.item("redstone_detector_tube_attachment", (properties) -> new TubeAttachmentItem("redstone_input", properties))
            .register();

    public static final ItemEntry<TubeAttachmentItem> TUBE_SENSOR = REGISTRATE.item("tube_sensor_attachment", (properties) -> new TubeAttachmentItem("tube_sensor", properties))
            .register();

    public static void register() {
    }
}
