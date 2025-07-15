package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.core.travel.TravelManager;
import com.pedrorok.hypertube.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
public record FinishPathPacket(UUID entityUuid) implements Packet<FinishPathPacket> {


    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(entityUuid);
    }

    public FinishPathPacket(FriendlyByteBuf buf) {
        this(buf.readUUID());
    }

    @Override
    public void execute(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TravelManager.finishTravel(entityUuid);
        });
        ctx.get().setPacketHandled(true);
    }
}
