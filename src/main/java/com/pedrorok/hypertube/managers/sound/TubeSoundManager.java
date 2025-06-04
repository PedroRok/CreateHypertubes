package com.pedrorok.hypertube.managers.sound;

import com.pedrorok.hypertube.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * @author Rok, Pedro Lucas nmm. Created on 04/06/2025
 * @project Create Hypertube
 */
public class TubeSoundManager {

    public static void tickClientPlayerSounds() {
        TravelSound.tickClientPlayerSounds();
    }

    public static class TubeAmbientSound {
        private boolean isClientNear;

        private TubeSound travelSound;

        public void enableClientPlayerSound(Entity e, Vec3 normal, double distance, boolean isOpen) {
            if (e != Minecraft.getInstance()
                    .getCameraEntity())
                return;

            if (distance > 32) {
                tickClientPlayerSounds();
                return;
            }
            isClientNear = true;


            float pitch = isOpen ? 1.5f : 0.5f;
            float maxVolume = Math.max(0, (float) (1.0 - (distance / 48)));

            if (travelSound == null || travelSound.isStopped()) {
                travelSound = new TubeSound(ModSounds.TRAVELING.get(), pitch);
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(travelSound);
            }
            travelSound.updateLocation(normal);
            travelSound.setPitch(pitch);
            travelSound.fadeIn(maxVolume);
        }

        public void tickClientPlayerSounds() {
            if (!isClientNear && travelSound != null)
                if (travelSound.isFaded())
                    travelSound.stopSound();
                else
                    travelSound.fadeOut();
            isClientNear = false;
        }

        public void stopSound() {
            if (travelSound != null) {
                travelSound.stopSound();
                travelSound = null;
            }
        }
    }

    public static class TravelSound {
        private static boolean isClientPlayerInTravel;

        private static TubeSound travelSound;

        public static void enableClientPlayerSound(Entity e, float maxVolume, float pitch) {
            if (e != Minecraft.getInstance()
                    .getCameraEntity())
                return;

            isClientPlayerInTravel = true;

            if (travelSound == null || travelSound.isStopped()) {
                travelSound = new TubeSound(ModSounds.TRAVELING.get(), pitch);
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(travelSound);
            }
            travelSound.setPitch(pitch);
            travelSound.fadeIn(maxVolume);
        }

        private static void tickClientPlayerSounds() {
            if (!isClientPlayerInTravel && travelSound != null)
                if (travelSound.isFaded())
                    travelSound.stopSound();
                else
                    travelSound.fadeOut();
            isClientPlayerInTravel = false;
        }
    }
}
