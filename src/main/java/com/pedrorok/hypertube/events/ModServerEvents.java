package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.commands.TestCommand;
import com.pedrorok.hypertube.managers.TravelManager;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/04/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = HypertubeMod.MOD_ID)
public class ModServerEvents {

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        new TestCommand().register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        TravelManager.playerTick(event.getEntity());
    }

    @SubscribeEvent
    public static void playerHitboxChangesWhenInHypertube(EntityEvent.Size event) {
        Entity entity = event.getEntity();
        if (!entity.isAddedToLevel())
            return;
        if (!TravelManager.hasHyperTubeData(entity))
            return;

        float scale;
        if(entity instanceof LivingEntity le) {
            scale = le.getScale();
        } else {
            scale = 1.0F;
        }

        event.setNewSize(EntityDimensions.fixed(0.5F * scale, 0.5F * scale));
    }
}
