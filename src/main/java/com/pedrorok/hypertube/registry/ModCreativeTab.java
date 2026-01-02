package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.HypertubeMod;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class ModCreativeTab {

    public static final ResourceKey<CreativeModeTab> TUBE_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            new ResourceLocation(HypertubeMod.MOD_ID, "create_hypertubes")
    );

    public static final CreativeModeTab TUBE_TAB = FabricItemGroup.builder()
            .title(Component.translatable("itemGroup." + HypertubeMod.MOD_ID))
            .icon(() -> new ItemStack(ModBlocks.HYPERTUBE.get()))
            .displayItems((parameters, output) -> {
                CreateRegistrate REGISTRATE = HypertubeMod.get();
                for (RegistryEntry<Item> entry : REGISTRATE.getAll(Registries.ITEM)) {
                    var item = entry.get();
                    if (item.asItem() == Items.AIR) continue;
                    if (ModItems.TUBE_SCANNER_UNFINISHED.is(item.asItem())) continue;
                    output.accept(item);
                }
            })
            .build();

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TUBE_TAB_KEY, TUBE_TAB);
    }

}