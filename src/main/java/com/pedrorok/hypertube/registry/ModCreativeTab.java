package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HypertubeMod.MOD_ID);

    /*public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MY_TAB =
            CREATIVE_MODE_TABS.register("create_hypertubes", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup." + HypertubeMod.MOD_ID))
                            .icon(ModBlocks.HYPERTUBE::asStack)
                            .displayItems((featureFlagSet, output) -> {
                                //output.accept(ModBlocks.HYPERTUBE.asStack());
                                //output.accept(ModBlocks.HYPERTUBE_ENTRANCE.asStack());
                            })
                            .build()
            );*/

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}