package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.managers.TravelManager;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * @author Rok, Pedro Lucas nmm. Created on 24/05/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(modid = HypertubeMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PlayerSyncEvents {
    
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncAllStatesToPlayer(serverPlayer);
            
            syncPlayerStateToAll(serverPlayer);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncAllStatesToPlayer(serverPlayer);
            syncPlayerStateToAll(serverPlayer);
        }
    }
    
    private static void syncAllStatesToPlayer(ServerPlayer targetPlayer) {
        for (ServerPlayer otherPlayer : targetPlayer.getServer().getPlayerList().getPlayers()) {
            if (otherPlayer != targetPlayer && TravelManager.hasHyperTubeData(otherPlayer)) {
                PacketDistributor.sendToPlayer(targetPlayer,
                    new ISyncPersistentData.PersistentDataPacket(otherPlayer));
            }
        }
    }
    
    public static void syncPlayerStateToAll(ServerPlayer sourcePlayer) {
        if (TravelManager.hasHyperTubeData(sourcePlayer)) {
            for (ServerPlayer otherPlayer : sourcePlayer.getServer().getPlayerList().getPlayers()) {
                if (otherPlayer != sourcePlayer) {
                    PacketDistributor.sendToPlayer(otherPlayer, 
                        new ISyncPersistentData.PersistentDataPacket(sourcePlayer));
                }
            }
        }
    }
}