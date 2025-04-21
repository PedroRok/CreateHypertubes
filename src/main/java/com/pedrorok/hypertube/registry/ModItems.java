package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
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
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HypertubeMod.MODID);

    public static final DeferredItem<BlockItem> HYPERTUBE_ITEM = ITEMS.register("hypertube",
            () -> new BlockItem(ModBlocks.HYPERTUBE.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}