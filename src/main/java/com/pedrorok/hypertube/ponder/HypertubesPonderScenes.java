package com.pedrorok.hypertube.ponder;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.ponder.scenes.AcceleratorScenes;
import com.pedrorok.hypertube.ponder.scenes.AttachmentScenes;
import com.pedrorok.hypertube.ponder.scenes.EntranceScenes;
import com.pedrorok.hypertube.ponder.scenes.TubeScenes;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModItems;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Rok, Pedro Lucas nmm. 27/01/2026
 * @project Create Hypertube
 */
public class HypertubesPonderScenes {

    static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(HypertubeMod.MOD_ID);

    public static void register() {
        HELPER.forComponents(ModBlocks.HYPERTUBE)
                .addStoryBoard("simple_tube", TubeScenes::simpleTube);
        HELPER.forComponents(ModBlocks.HYPERTUBE_ENTRANCE)
                .addStoryBoard("entrance", EntranceScenes::entranceScene);
        HELPER.forComponents(ModBlocks.HYPER_ACCELERATOR)
                .addStoryBoard("accelerator", AcceleratorScenes::acceleratorScene);
        HELPER.forComponents(ModItems.REDSTONE_DETECTOR, ModItems.TUBE_SCANNER)
                .addStoryBoard("attachment", AttachmentScenes::attachmentScene);
    }

    public static boolean isAnyPonderScreenOpen() {
        return Minecraft.getInstance().screen instanceof PonderUI;
    }
}
