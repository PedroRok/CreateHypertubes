package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HypertubeMod.MODID);

    public static final Supplier<BlockEntityType<HyperEntranceBlockEntity>> HYPERTUBE_ENTRANCE_ENTITY = BLOCK_ENTITY_TYPES.register(
            "hypertube_entrance_entity",
            () ->
                    BlockEntityType.Builder.of(HyperEntranceBlockEntity::new, ModBlocks.HYPERTUBE_ENTRANCE.get()).build(null)
    );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
