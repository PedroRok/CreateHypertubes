package com.pedrorok.hypertube.core.sound;

import com.pedrorok.hypertube.core.camera.DetachedCameraController;
import com.pedrorok.hypertube.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 04/06/2025
 * @project Create Hypertube
 */
public class TubeSoundManager {

    private static final Map<UUID, TubeAmbientSound> ambientSounds = new HashMap<>();

    @OnlyIn(Dist.CLIENT)
    public static void tickClientPlayerSounds() {
        TravelSound.tickClientPlayerSounds();
    }

    @OnlyIn(Dist.CLIENT)
    public static TubeAmbientSound getAmbientSound(UUID uuid) {
        return ambientSounds.computeIfAbsent(uuid, k -> new TubeAmbientSound());
    }

    @OnlyIn(Dist.CLIENT)
    public static void removeAmbientSound(UUID uuid) {
        TubeAmbientSound sound = ambientSounds.remove(uuid);
        if (sound != null) {
            sound.stopSound();
        }
    }

    public static class TubeAmbientSound {
        private boolean isClientNear;

        private TubeSound travelSound;

        @OnlyIn(Dist.CLIENT)
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

        @OnlyIn(Dist.CLIENT)
        public void tickClientPlayerSounds() {
            if (!isClientNear && travelSound != null)
                if (travelSound.isFaded())
                    travelSound.stopSound();
                else
                    travelSound.fadeOut();
            isClientNear = false;
        }

        @OnlyIn(Dist.CLIENT)
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

        @OnlyIn(Dist.CLIENT)
        public static void enableClientPlayerSound(Entity e, float maxVolume, float pitch) {
            if (e != Minecraft.getInstance()
                    .getCameraEntity())
                return;

            isClientPlayerInTravel = true;

            float cameraYaw = Math.abs(DetachedCameraController.get().getYaw());
            float cameraPitch = Math.abs(DetachedCameraController.get().getPitch());

            float yRot = Math.abs(Minecraft.getInstance().player.getYRot());
            float xRot = Math.abs(Minecraft.getInstance().player.getXRot());

            float equalYaw = Math.abs(cameraYaw - yRot);
            float equalPitch = Math.abs(cameraPitch - xRot);

            if (travelSound == null || travelSound.isStopped()) {
                travelSound = new TubeSound(ModSounds.TRAVELING.get(), pitch);
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(travelSound);
            }
            boolean isCameraInside = equalYaw < 12 && equalPitch < 12;
            travelSound.setPitch(isCameraInside ? pitch : 1.5f);
            travelSound.fadeIn(maxVolume * (isCameraInside ? 1.5f : 0.8f));
        }

        @OnlyIn(Dist.CLIENT)
        private static void tickClientPlayerSounds() {
            if (!isClientPlayerInTravel && travelSound != null)
                if (travelSound.isFaded())
                    travelSound.stopSound();
                else
                    travelSound.fadeOut();
            isClientPlayerInTravel = false;
        }
    }


    public static void playTubeSuctionSound(LivingEntity entity, Vec3 pos) {
        RandomSource random = entity.level().random;
        float pitch = 0.8F + random.nextFloat() * 0.4F;
        int seed = random.nextInt(1000);
        for (Player oPlayer : entity.level().players()) {
            ((ServerPlayer) oPlayer).connection.send(new ClientboundSoundPacket(ModSounds.HYPERTUBE_SUCTION,
                    SoundSource.BLOCKS, pos.x, pos.y, pos.z, 1, pitch, seed));
        }
    }
}
