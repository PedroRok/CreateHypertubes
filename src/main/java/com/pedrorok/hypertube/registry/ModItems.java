package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.items.HypertubeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author Rok, Pedro Lucas nmm. Created on 17/04/2025
 * @project Create Hypertube
 */
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HypertubeMod.MOD_ID);

    public static final DeferredItem<BlockItem> HYPERTUBE_ITEM = ITEMS.register("hypertube",
            () -> new HypertubeItem(ModBlocks.HYPERTUBE.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> HYPERTUBE_ENTRANCE_ITEM = ITEMS.register("hypertube_entrance",
            () -> new BlockItem(ModBlocks.HYPERTUBE_ENTRANCE.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}