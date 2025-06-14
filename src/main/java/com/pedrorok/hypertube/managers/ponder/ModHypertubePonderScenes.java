package com.pedrorok.hypertube.managers.ponder;

import com.pedrorok.hypertube.managers.ponder.scenes.HypertubeScenes;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Rok, Pedro Lucas nmm. Created on 05/06/2025
 * @project Create Hypertube
 */
public class ModHypertubePonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(ModBlocks.HYPERTUBE)
                .addStoryBoard("hypertube", HypertubeScenes::tube);
    }
}
