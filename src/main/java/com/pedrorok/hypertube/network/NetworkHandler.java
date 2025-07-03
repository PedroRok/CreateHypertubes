package com.pedrorok.hypertube.network;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.network.packets.PlayerTravelDirDataPacket;
import com.pedrorok.hypertube.network.packets.SyncPersistentDataPacket;
import com.pedrorok.hypertube.network.packets.MovePathPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * @author Rok, Pedro Lucas nmm. Created on 18/06/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(modid = HypertubeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                SyncPersistentDataPacket.TYPE,
                SyncPersistentDataPacket.STREAM_CODEC,
                SyncPersistentDataPacket::handle
        );
        registrar.playToClient(
                PlayerTravelDirDataPacket.TYPE,
                PlayerTravelDirDataPacket.STREAM_CODEC,
                PlayerTravelDirDataPacket::handle
        );
        registrar.playToClient(
                MovePathPacket.TYPE,
                MovePathPacket.STREAM_CODEC,
                MovePathPacket::handle
        );
    }


    public static void sendToTracking(Packet<?> packet, ServerLevel level, Entity entity) {
        for (ServerPlayer player : level.getPlayers(p -> p != null && p.distanceToSqr(entity) < 1024)) {
            player.connection.send(packet);
        }
    }

    public static void sendToTracking(MovePathPacket packet, ServerLevel level, Entity entity) {
        for (ServerPlayer player : level.getPlayers(p -> p.hasLineOfSight(entity))) {
            PacketDistributor.sendToPlayer(player, packet);
        }
    }
}
