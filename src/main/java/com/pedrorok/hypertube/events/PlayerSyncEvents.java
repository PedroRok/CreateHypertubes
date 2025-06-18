package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.managers.travel.TravelManager;
import com.pedrorok.hypertube.network.packets.SyncPersistentDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Optionally handle player logout logic here, if needed
            // For example, you might want to clear their data or notify other players
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
            SyncPersistentDataPacket payload = SyncPersistentDataPacket.create(otherPlayer);
            if (otherPlayer == targetPlayer || !TravelManager.hasHyperTubeData(otherPlayer)) continue;
            PacketDistributor.sendToPlayer(targetPlayer, payload);
        }
    }

    public static void syncPlayerStateToAll(ServerPlayer sourcePlayer) {
        if (!TravelManager.hasHyperTubeData(sourcePlayer)) return;
        SyncPersistentDataPacket payload = SyncPersistentDataPacket.create(sourcePlayer);
        for (ServerPlayer otherPlayer : sourcePlayer.getServer().getPlayerList().getPlayers()) {
            if (otherPlayer == sourcePlayer) continue;
            PacketDistributor.sendToPlayer(otherPlayer, payload);
        }

    }
}