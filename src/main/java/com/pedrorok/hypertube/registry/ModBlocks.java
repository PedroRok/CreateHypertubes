package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperAcceleratorBlock;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 17/04/2025
 * @project Create Hypertube
 */
public class ModBlocks {

    public static final BlockBehaviour.Properties PROPERTIES = BlockBehaviour.Properties.of()
            .destroyTime(1.0f)
            .explosionResistance(10.0f)
            .sound(SoundType.METAL)
            .noOcclusion()
            .isViewBlocking((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false);

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, HypertubeMod.MOD_ID);

    public static final DeferredHolder<Block, HypertubeBlock> HYPERTUBE =
            BLOCKS.register("hypertube", () -> new HypertubeBlock(PROPERTIES));

    public static final DeferredHolder<Block, HyperEntranceBlock> HYPERTUBE_ENTRANCE =
            BLOCKS.register("hypertube_entrance", () -> new HyperEntranceBlock(PROPERTIES));

    public static final DeferredHolder<Block, HyperAcceleratorBlock> HYPER_ACCELERATOR =
            BLOCKS.register("hypertube_accelerator", () -> new HyperAcceleratorBlock(PROPERTIES));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}