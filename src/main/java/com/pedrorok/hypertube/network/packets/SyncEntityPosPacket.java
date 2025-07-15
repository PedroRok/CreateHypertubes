package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.core.travel.ClientTravelPathMover;
import com.pedrorok.hypertube.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 15/07/2025
 * @project Create Hypertube
 */
public record SyncEntityPosPacket(int entityId, int segment) implements Packet<SyncEntityPosPacket> {

    public SyncEntityPosPacket(FriendlyByteBuf buf) {
        this(
                buf.readInt(),
                buf.readInt()
        );
    }

    public static SyncEntityPosPacket create(Entity entity, int segment) {
        return new SyncEntityPosPacket(
                entity.getId(),
                segment
        );
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(segment);
    }

    @Override
    public void execute(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(this);
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(SyncEntityPosPacket packet) {
        ClientTravelPathMover.updateSegment(packet.entityId, packet.segment);
    }

}
