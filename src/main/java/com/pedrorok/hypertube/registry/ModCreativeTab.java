package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypetubeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HypetubeMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MY_TAB =
            CREATIVE_MODE_TABS.register("create_hypertubes", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup." + HypetubeMod.MODID))
                            .icon(ModItems.HYPERTUBE_ITEM::toStack)
                            .displayItems((featureFlagSet, output) -> {
                                output.accept(new ItemStack(ModItems.HYPERTUBE_ITEM.get()));
                            })
                            .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}