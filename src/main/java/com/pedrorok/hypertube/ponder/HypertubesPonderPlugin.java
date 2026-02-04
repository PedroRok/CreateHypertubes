package com.pedrorok.hypertube.ponder;

import com.pedrorok.hypertube.HypertubeMod;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rok, Pedro Lucas nmm. 27/01/2026
 * @project Create Hypertube
 */
public class HypertubesPonderPlugin implements PonderPlugin {
    @Override
    public @NotNull String getModId() {
        return HypertubeMod.MOD_ID;
    }

    @Override
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        HypertubesPonderScenes.register(helper);
    }

    public static boolean isAnyPonderScreenOpen() {
        return Minecraft.getInstance().screen instanceof PonderUI;
    }

    @Override
    public void registerTags(@NotNull PonderTagRegistrationHelper<ResourceLocation> helper) {
        HypertubesPonderTags.register(helper);
    }
}
