package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.core.travel.TravelManager;
import com.pedrorok.hypertube.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. 29/06/2026
 * @project Create Hypertube
 */
public record EscapeTubePacket() implements Packet<EscapeTubePacket> {

    public EscapeTubePacket(FriendlyByteBuf buf) {
        this();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
    }

    @Override
    public void execute(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender == null) return;
            TravelManager.finishTravel(sender);
        });
        ctx.get().setPacketHandled(true);
    }
}
