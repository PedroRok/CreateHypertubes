package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
@Mod.EventBusSubscriber(modid = HypertubeMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HypertubeMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TUBE_TAB =
            CREATIVE_MODE_TABS.register("create_hypertubes", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup." + HypertubeMod.MOD_ID))
                            .icon(ModBlocks.HYPERTUBE::asStack)
                            .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(TUBE_TAB.getKey())) {
            CreateRegistrate REGISTRATE = HypertubeMod.get();
            for (RegistryEntry<Block> entry : REGISTRATE.getAll(Registries.BLOCK)) {
                var block = entry.get();
                if (block.asItem() == Items.AIR) continue;
                event.accept(block);
            }
        }
    }

}