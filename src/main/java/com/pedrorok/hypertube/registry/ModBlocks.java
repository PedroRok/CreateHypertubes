package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author Rok, Pedro Lucas nmm. Created on 17/04/2025
 * @project Create Hypertube
 */
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HypertubeMod.MODID);

    public static final DeferredBlock<HypertubeBlock> HYPERTUBE = BLOCKS.register("hypertube", HypertubeBlock::new);

    public static final DeferredBlock<HyperEntranceBlock> HYPERTUBE_ENTRANCE = BLOCKS.register("hypertube_entrance", HyperEntranceBlock::new);


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
