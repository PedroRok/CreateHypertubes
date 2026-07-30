package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.core.data.MoveDirection;
import com.pedrorok.hypertube.core.travel.TravelManager;
import com.pedrorok.hypertube.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 25/07/2026
 * @project Create Hypertube
 */
public record MoveDirectionPacket(MoveDirection direction) implements Packet<MoveDirectionPacket> {

    public MoveDirectionPacket(FriendlyByteBuf buf) {
        this(buf.readEnum(MoveDirection.class));
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(direction);
    }

    @Override
    public void execute(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender == null) return;
            TravelManager.changeDirection(direction, sender.getUUID(), sender.level());
        });
        ctx.get().setPacketHandled(true);
    }
}
