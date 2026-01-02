package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.mixin.core.EntityPersistentData;
import com.pedrorok.hypertube.network.NetworkHandler;
import com.pedrorok.hypertube.core.travel.TravelManager;
import com.pedrorok.hypertube.network.packets.SyncPersistentDataPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * @author Rok, Pedro Lucas nmm. Created on 24/05/2025
 * @project Create Hypertube
 */
public class PlayerSyncEvents {

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer serverPlayer = handler.player;
            syncAllStatesToPlayer(serverPlayer);
            syncPlayerStateToAll(serverPlayer, false);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer serverPlayer = handler.player;
            TravelManager.finishTravel(serverPlayer);
        });
    }

    private static void syncAllStatesToPlayer(ServerPlayer targetPlayer) {
        for (ServerPlayer otherPlayer : targetPlayer.getServer().getPlayerList().getPlayers()) {
            if (otherPlayer != targetPlayer && TravelManager.hasHyperTubeData(otherPlayer)) {
                NetworkHandler.sendToClient(
                        targetPlayer,
                        new SyncPersistentDataPacket(otherPlayer.getId(), ((EntityPersistentData) otherPlayer).getPersistentData())
                );
            }
        }
    }

    public static void syncPlayerStateToAll(LivingEntity sourcePlayer, boolean force) {
        if (!TravelManager.hasHyperTubeData(sourcePlayer) && !force) return;
        for (ServerPlayer otherPlayer : sourcePlayer.getServer().getPlayerList().getPlayers()) {
            if (otherPlayer != sourcePlayer) {
                NetworkHandler.sendToClient(
                        otherPlayer,
                        new SyncPersistentDataPacket(sourcePlayer.getId(), ((EntityPersistentData) sourcePlayer).getPersistentData())
                );
            }
        }
    }
}