package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.managers.placement.TubePlacement;
import com.pedrorok.hypertube.managers.travel.TravelConstants;
import com.pedrorok.hypertube.managers.travel.TravelManager;
import com.pedrorok.hypertube.utils.TubeUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.level.BlockEvent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
@SuppressWarnings("removal")
@Mod.EventBusSubscriber(bus = EventBusSubscriber.Bus.FORGE, modid = HypertubeMod.MOD_ID)
public class ModServerEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        TravelManager.playerTick(event.player);
        if (event.player.level().isClientSide) {
            return;
        }
        TubePlacement.tickPlayerServer(event.player);
    }

    @SubscribeEvent
    public static void playerHitboxChangesWhenInHypertube(EntityEvent.Size event) {
        Entity entity = event.getEntity();
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
    public static void onEntityHurt(LivingDamageEvent event) {
        if (event.getPhase() != EventPriority.HIGH) return;
        LivingEntity entity = event.getEntity();

        if (!entity.getPersistentData().getBoolean(TravelConstants.IMMUNITY_TAG)) return;
        entity.getPersistentData().putBoolean(TravelConstants.IMMUNITY_TAG, false);

        if (entity.getPersistentData().getLong(TravelConstants.LAST_TRAVEL_TIME) < System.currentTimeMillis()) return;
        event.setAmount(0);
        event.setCanceled(true);
        entity.hurtMarked = true;
    }
}
