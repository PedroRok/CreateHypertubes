package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.items.TubeAttachmentItem;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/11/2025
 * @project Create Hypertube
 */
public class ModItems {

    private static final CreateRegistrate REGISTRATE = HypertubeMod.get();

    public static final ItemEntry<TubeAttachmentItem> REDSTONE_DETECTOR = REGISTRATE.item("redstone_detector_tube_attachment", (properties) -> new TubeAttachmentItem("redstone_input", properties))
            .register();

    public static final ItemEntry<TubeAttachmentItem> TUBE_SCANNER = REGISTRATE.item("tube_scanner_attachment", (properties) -> new TubeAttachmentItem("tube_scanner", properties))
            .register();

    public static final ItemEntry<Item> TUBE_SCANNER_UNFINISHED = REGISTRATE.item("tube_scanner_unfinished", Item::new)
            .register();

    public static void register() {
    }
}
