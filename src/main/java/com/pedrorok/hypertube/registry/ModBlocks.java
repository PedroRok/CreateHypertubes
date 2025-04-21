package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypetubeMod;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author Rok, Pedro Lucas nmm. Created on 17/04/2025
 * @project Create Hypertube
 */
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HypetubeMod.MODID);

    public static final DeferredBlock<HypertubeBlock> HYPERTUBE = BLOCKS.register("hypertube", HypertubeBlock::new);


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
