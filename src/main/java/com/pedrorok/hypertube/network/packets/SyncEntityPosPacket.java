package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.core.travel.ClientTravelPathMover;
import com.pedrorok.hypertube.network.ClientBoundPacket;
import com.pedrorok.hypertube.network.Packet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

/**
 * @author Rok, Pedro Lucas nmm. Created on 15/07/2025
 * @project Create Hypertube
 */
public record SyncEntityPosPacket(int entityId, int segment) implements Packet<SyncEntityPosPacket>, ClientBoundPacket {

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
    @Environment(EnvType.CLIENT)
    public void executeOnClient() {
        ClientTravelPathMover.updateSegment(this.entityId, this.segment);
    }

}
