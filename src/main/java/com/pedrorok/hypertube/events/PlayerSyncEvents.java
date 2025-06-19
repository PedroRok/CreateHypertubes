package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.managers.travel.TravelManager;
import com.pedrorok.hypertube.network.NetworkHandler;
import com.pedrorok.hypertube.network.packets.SyncPersistentDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

/**
 * @author Rok, Pedro Lucas nmm. Created on 24/05/2025
 * @project Create Hypertube
 */
@Mod.EventBusSubscriber(modid = HypertubeMod.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public class PlayerSyncEvents {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncAllStatesToPlayer(serverPlayer);
            syncPlayerStateToAll(serverPlayer, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TravelManager.finishTravel(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncAllStatesToPlayer(serverPlayer);
            syncPlayerStateToAll(serverPlayer, false);
        }
    }

    private static void syncAllStatesToPlayer(ServerPlayer targetPlayer) {
        for (ServerPlayer otherPlayer : targetPlayer.getServer().getPlayerList().getPlayers()) {
            if (otherPlayer != targetPlayer && TravelManager.hasHyperTubeData(otherPlayer)) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> targetPlayer),
                        new SyncPersistentDataPacket(targetPlayer.getId(), targetPlayer.getPersistentData())
                );
            }
        }
    }

    public static void syncPlayerStateToAll(ServerPlayer sourcePlayer, boolean force) {
        if (!TravelManager.hasHyperTubeData(sourcePlayer) && !force) return;
        for (ServerPlayer otherPlayer : sourcePlayer.getServer().getPlayerList().getPlayers()) {
            if (otherPlayer != sourcePlayer) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> sourcePlayer),
                        new SyncPersistentDataPacket(sourcePlayer.getId(), sourcePlayer.getPersistentData())
                );
            }
        }
    }
}