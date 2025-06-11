package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.managers.TravelManager;
import com.pedrorok.hypertube.managers.placement.ResponseDTO;
import com.pedrorok.hypertube.managers.placement.TubePlacement;
import com.pedrorok.hypertube.utils.MessageUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = HypertubeMod.MOD_ID)
public class ModServerEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        TravelManager.playerTick(event.getEntity());
        if (event.getEntity().level().isClientSide) {
            return;
        }
        TubePlacement.tickPlayerServer(event.getEntity());
    }

    @SubscribeEvent
    public static void playerHitboxChangesWhenInHypertube(EntityEvent.Size event) {
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

        if (TubePlacement.checkPlayerPlacingBlock(player, (Level) event.getLevel(), event.getPos())) {
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
}
