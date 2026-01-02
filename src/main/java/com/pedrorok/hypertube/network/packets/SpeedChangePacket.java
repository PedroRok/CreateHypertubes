package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.core.travel.ClientTravelPathMover;
import com.pedrorok.hypertube.network.ClientBoundPacket;
import com.pedrorok.hypertube.network.Packet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;

/**
 * @author Rok, Pedro Lucas nmm. Created on 09/08/2025
 * @project Create Hypertube
 */
public record SpeedChangePacket(int entityId, double newSpeed) implements Packet<SpeedChangePacket>, ClientBoundPacket {

    public SpeedChangePacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readDouble());
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeDouble(this.newSpeed);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void executeOnClient() {
        ClientTravelPathMover.updateEntitySpeed(this);
    }
}