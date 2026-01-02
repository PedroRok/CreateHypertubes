package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.core.travel.TravelManager;
import com.pedrorok.hypertube.network.Packet;
import com.pedrorok.hypertube.network.ServerBoundPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 10/08/2025
 * @project Create Hypertube
 */
public record ActionPointReachPacket(UUID entityId,
                                     BlockPos pos) implements Packet<ActionPointReachPacket>, ServerBoundPacket {

    public ActionPointReachPacket(FriendlyByteBuf buf) {
        this(
                buf.readUUID(),
                buf.readBlockPos()
        );
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.entityId);
        buf.writeBlockPos(this.pos);
    }

    @Override
    public void executeOnServer(ServerPlayer player) {
        TravelManager.actionPointReach(this.entityId, this.pos);
    }
}
