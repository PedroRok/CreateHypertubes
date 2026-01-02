package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.config.ServerConfig;
import com.pedrorok.hypertube.core.placement.TubePlacement;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.core.travel.TravelManager;
import com.pedrorok.hypertube.mixin.core.EntityPersistentData;
import com.pedrorok.hypertube.utils.TubeUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
public class ModServerEvents {

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ServerConfig.get().init();
        });

        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (world.isClientSide) return;
            for (Entity entity : world.getAllEntities()) {
                if (entity instanceof LivingEntity living) {
                    if (!ServerConfig.canEntityTravel(living.getType())) continue;
                    TravelManager.entityTick(living);
                    if (living instanceof Player player) {
                        TubePlacement.tickPlayerServer(player);
                    }
                }
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide) return InteractionResult.PASS;
            if (TravelManager.hasHyperTubeData(player)) {
                return InteractionResult.FAIL;
            }
            if (TubeUtils.checkPlayerPlacingBlock(player, world, hitResult.getBlockPos())) {
                return InteractionResult.PASS;
            }
            return InteractionResult.FAIL;
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (TravelManager.hasHyperTubeData(player)) {
                return false;
            }
            return true;
        });
    }

    public static void entityHitboxChangesWhenInHypertube(Entity entity) {
        if (!TravelManager.hasHyperTubeData(entity))
            return;

        entity.setBoundingBox(EntityDimensions.fixed(0.5F, 0.5F).makeBoundingBox(entity.position()));
        if (entity.level().isClientSide) return;
        entity.setPose(Pose.CROUCHING);
    }

    public static float onEntityHurt(LivingEntity entity, float amount) {
        if (entity.level().isClientSide) return amount;
        if (TravelManager.hasHyperTubeData(entity)) {
            return 0;
        }

        if (!((EntityPersistentData) entity).getPersistentData().getBoolean(TravelConstants.IMMUNITY_TAG))
            return amount;
        ((EntityPersistentData) entity).getPersistentData().putBoolean(TravelConstants.IMMUNITY_TAG, false);

        if (((EntityPersistentData) entity).getPersistentData().getLong(TravelConstants.LAST_TRAVEL_TIME) < System.currentTimeMillis())
            return amount;
        entity.hurtMarked = true;
        return 0;
    }
}
