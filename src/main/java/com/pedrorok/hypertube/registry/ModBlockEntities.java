package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.client.renderer.EntranceBlockEntityRenderer;
import com.pedrorok.hypertube.client.renderer.HypertubeBlockEntityRenderer;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class ModBlockEntities {
    private static final CreateRegistrate REGISTRATE = HypertubeMod.get();

    public static final BlockEntityEntry<HyperEntranceBlockEntity> HYPERTUBE_ENTRANCE = REGISTRATE
            .blockEntity("hypertube_entrance_entity", HyperEntranceBlockEntity::new)
            .renderer(() -> EntranceBlockEntityRenderer::new)
            .validBlocks(ModBlocks.HYPERTUBE_ENTRANCE)
            .register();

    public static final BlockEntityEntry<HypertubeBlockEntity> HYPERTUBE = REGISTRATE
            .blockEntity("hypertube_entity", HypertubeBlockEntity::new)
            .renderer(() -> HypertubeBlockEntityRenderer::new)
            .validBlocks(ModBlocks.HYPERTUBE)
            .register();

    public static void register() {
    }
}
