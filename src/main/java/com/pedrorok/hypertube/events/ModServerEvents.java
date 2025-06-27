package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.config.ServerConfig;
import com.pedrorok.hypertube.core.placement.TubePlacement;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.core.travel.TravelManager;
import com.pedrorok.hypertube.utils.TubeUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = HypertubeMod.MOD_ID)
public class ModServerEvents {

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        ServerConfig.get().init();
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (!TravelConstants.ENTITIES_CAN_TRAVEL.containsKey(living.getType())) return;
        TravelManager.entityTick(living);
        if (event.getEntity().level().isClientSide) {
            return;
        }
        if (!(living instanceof Player player)) return;
        TubePlacement.tickPlayerServer(player);
    }

    @SubscribeEvent
    public static void entityHitBoxChangesWhenInHypertube(EntityEvent.Size event) {
        Entity entity = event.getEntity();
        if (!entity.isAddedToLevel())
            return;
        if (!TravelManager.hasHyperTubeData(entity))
            return;

        float scale;
        if (entity instanceof LivingEntity le) {
            scale = le.getScale();
        } else {
            scale = 1.0F;
        }

        event.setNewSize(EntityDimensions.fixed(0.5F * scale, 0.5F * scale));
    }


    @SubscribeEvent
    public static void playerPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() == null) return;

        if (TravelManager.hasHyperTubeData(event.getEntity())) {
            event.setCanceled(true);
            return;
        }
        if (event.getEntity().level().isClientSide) return;

        if (!(event.getEntity() instanceof Player player)) return;

        if (TubeUtils.checkPlayerPlacingBlock(player, (Level) event.getLevel(), event.getPos())) {
            return;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void playerBreakBlock(BlockEvent.BreakEvent event) {
        if (!TravelManager.hasHyperTubeData(event.getPlayer()))
            return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) return;
        LivingEntity entity = event.getEntity();

        if (!entity.getPersistentData().getBoolean(TravelConstants.IMMUNITY_TAG)) return;
        entity.getPersistentData().putBoolean(TravelConstants.IMMUNITY_TAG, false);

        if (entity.getPersistentData().getLong(TravelConstants.LAST_TRAVEL_TIME) < System.currentTimeMillis()) return;
        event.getContainer().setNewDamage(0);
        entity.hurtMarked = true;
    }
}
