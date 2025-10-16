package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.items.HypertubeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author Rok, Pedro Lucas nmm. Created on 17/04/2025
 * @project Create Hypertube
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(Registries.ITEM, HypertubeMod.MOD_ID);

    public static final DeferredHolder<Item, HypertubeItem> HYPERTUBE = 
            ITEMS.register("hypertube", () -> new HypertubeItem(ModBlocks.HYPERTUBE.getDelegate().value(), new Item.Properties()));

    public static final DeferredHolder<Item, BlockItem> HYPERTUBE_ENTRANCE = 
            ITEMS.register("hypertube_entrance", () -> new BlockItem(ModBlocks.HYPERTUBE_ENTRANCE.getDelegate().value(), new Item.Properties()));

    public static final DeferredHolder<Item, BlockItem> HYPER_ACCELERATOR = 
            ITEMS.register("hypertube_accelerator", () -> new BlockItem(ModBlocks.HYPER_ACCELERATOR.getDelegate().value(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}