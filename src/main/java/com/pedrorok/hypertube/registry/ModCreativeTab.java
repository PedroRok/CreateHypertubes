
package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(modid = HypertubeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HypertubeMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TUBE_TAB =
            CREATIVE_MODE_TABS.register("create_hypertubes", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup." + HypertubeMod.MOD_ID))
                            .icon(() -> ModBlocks.HYPERTUBE.get().getItem().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                output.accept(ModItems.HYPERTUBE.get());
                                output.accept(ModItems.HYPERTUBE_ENTRANCE.get());
                                output.accept(ModItems.HYPER_ACCELERATOR.get());
                            })
                            .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}