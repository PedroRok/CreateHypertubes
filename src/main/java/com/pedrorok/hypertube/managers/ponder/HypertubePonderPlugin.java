package com.pedrorok.hypertube.managers.ponder;

import com.pedrorok.hypertube.HypertubeMod;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rok, Pedro Lucas nmm. Created on 05/06/2025
 * @project Create Hypertube
 */
public class HypertubePonderPlugin implements PonderPlugin {

    @Override
    public @NotNull String getModId() {
        return HypertubeMod.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ModHypertubePonderScenes.register(helper);
    }
}

