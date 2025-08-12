package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.core.travel.ClientTravelPathMover;
import com.pedrorok.hypertube.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 09/08/2025
 * @project Create Hypertube
 */
public record SpeedChangePacket(int entityId, double newSpeed) implements Packet<SpeedChangePacket> {

    public SpeedChangePacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readDouble());
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeDouble(this.newSpeed);
    }

    @Override
    public void execute(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientTravelPathMover.updateEntitySpeed(this);
        });
        ctx.get().setPacketHandled(true);
    }
}