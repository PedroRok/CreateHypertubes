package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.core.travel.TravelManager;
import com.pedrorok.hypertube.network.Packet;
import com.pedrorok.hypertube.network.ServerBoundPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
public record FinishPathPacket(UUID entityUuid) implements Packet<FinishPathPacket>, ServerBoundPacket {

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(entityUuid);
    }

    public FinishPathPacket(FriendlyByteBuf buf) {
        this(buf.readUUID());
    }

    @Override
    public void executeOnServer(ServerPlayer player) {
        TravelManager.finishTravel(entityUuid);
    }
}
