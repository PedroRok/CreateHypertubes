package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.blockentities.HyperAcceleratorBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HypertubeMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HyperEntranceBlockEntity>> HYPERTUBE_ENTRANCE =
            BLOCK_ENTITIES.register("hypertube_entrance_entity", () ->
                    BlockEntityType.Builder.of(
                            HyperEntranceBlockEntity::new,
                            ModBlocks.HYPERTUBE_ENTRANCE.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HypertubeBlockEntity>> HYPERTUBE =
            BLOCK_ENTITIES.register("hypertube_entity", () ->
                    BlockEntityType.Builder.of(
                            HypertubeBlockEntity::new,
                            ModBlocks.HYPERTUBE.get()
                    ).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HyperAcceleratorBlockEntity>> HYPER_ACCELERATOR =
            BLOCK_ENTITIES.register("hyper_accelerator_entity", () ->
                    BlockEntityType.Builder.of(
                            HyperAcceleratorBlockEntity::new,
                            ModBlocks.HYPER_ACCELERATOR.get()
                    ).build(null)
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}