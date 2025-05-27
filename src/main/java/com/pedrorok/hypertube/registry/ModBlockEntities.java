package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.client.BezierTextureRenderer;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class ModBlockEntities {
    private static final CreateRegistrate REGISTRATE = HypertubeMod.get();

    public static final BlockEntityEntry<HyperEntranceBlockEntity> HYPERTUBE_ENTRANCE = REGISTRATE
            .blockEntity("hypertube_entrance_entity", HyperEntranceBlockEntity::new)
            .validBlocks(ModBlocks.HYPERTUBE_ENTRANCE)
            .register();

    public static final BlockEntityEntry<HypertubeBlockEntity> HYPERTUBE = REGISTRATE
            .blockEntity("hypertube_entity", HypertubeBlockEntity::new)
            .renderer(() -> BezierTextureRenderer::new)
            .validBlocks(ModBlocks.HYPERTUBE)
            .register();

    public static void register() {
    }
}
