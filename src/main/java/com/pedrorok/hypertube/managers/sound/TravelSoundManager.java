package com.pedrorok.hypertube.managers.sound;

import com.pedrorok.hypertube.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * @author Rok, Pedro Lucas nmm. Created on 04/06/2025
 * @project Create Hypertube
 */
public class TravelSoundManager {

    private static boolean isClientPlayerInTravel4;

    private static TubeTravelSound travelSound;

    public static void enableClientPlayerSound(Entity e, float maxVolume, float pitch) {
        if (e != Minecraft.getInstance()
                .getCameraEntity())
            return;

        isClientPlayerInTravel4 = true;

        if (travelSound == null || travelSound.isStopped()) {
            travelSound = new TubeTravelSound(ModSounds.TRAVELING.get(), pitch);
            Minecraft.getInstance()
                    .getSoundManager()
                    .play(travelSound);
        }
        travelSound.setPitch(pitch);
        travelSound.fadeIn(maxVolume);
    }

    public static void tickClientPlayerSounds() {
        if (!isClientPlayerInTravel4 && travelSound != null)
            if (travelSound.isFaded())
                travelSound.stopSound();
            else
                travelSound.fadeOut();
        isClientPlayerInTravel4 = false;
    }
}
