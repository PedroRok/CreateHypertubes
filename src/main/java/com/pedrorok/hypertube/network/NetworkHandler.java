package com.pedrorok.hypertube.network;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.network.packets.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
                EntityTravelDirDataPacket.TYPE,
                EntityTravelDirDataPacket.STREAM_CODEC,
                EntityTravelDirDataPacket::handle
        );
        registrar.playToServer(FinishPathPacket.TYPE,
                FinishPathPacket.STREAM_CODEC,
                FinishPathPacket::handle
        );
        registrar.playToClient(
                MovePathPacket.TYPE,
                MovePathPacket.STREAM_CODEC,
                MovePathPacket::handle
        );
        registrar.playToClient(
                SyncEntityPosPacket.TYPE,
                SyncEntityPosPacket.STREAM_CODEC,
                SyncEntityPosPacket::handle
        );
    }
}
