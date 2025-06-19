package com.pedrorok.hypertube.events;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.managers.travel.TravelManager;
import com.simibubi.create.AllPackets;
import com.pedrorok.hypertube.network.packets.SyncPersistentDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerEvent;
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
            if (otherPlayer != targetPlayer && TravelManager.hasHyperTubeData(otherPlayer)) {
                AllPackets.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> targetPlayer), new ISyncPersistentData.PersistentDataPacket(targetPlayer));

            }
        }
    }

    public static void syncPlayerStateToAll(ServerPlayer sourcePlayer) {
        if (TravelManager.hasHyperTubeData(sourcePlayer)) {
            for (ServerPlayer otherPlayer : sourcePlayer.getServer().getPlayerList().getPlayers()) {
                if (otherPlayer != sourcePlayer) {
                    AllPackets.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> sourcePlayer), new ISyncPersistentData.PersistentDataPacket(sourcePlayer));

                }
            }
        }

    }
}