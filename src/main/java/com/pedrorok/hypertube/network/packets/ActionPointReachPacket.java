package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.core.travel.TravelManager;
import com.pedrorok.hypertube.network.Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 10/08/2025
 * @project Create Hypertube
 */
public record ActionPointReachPacket(UUID entityId, BlockPos pos) implements Packet<ActionPointReachPacket> {

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
    public void execute(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TravelManager.actionPointReach(this.entityId, this.pos);
        });
        ctx.get().setPacketHandled(true);
    }
}
