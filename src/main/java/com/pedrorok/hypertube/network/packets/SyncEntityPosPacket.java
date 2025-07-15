package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.travel.ClientTravelPathMover;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rok, Pedro Lucas nmm. Created on 15/07/2025
 * @project Create Hypertube
 */
public record SyncEntityPosPacket(int entityId, int segment) implements CustomPacketPayload {

    public static final Type<SyncEntityPosPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "sync_entity_pos")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntityPosPacket> STREAM_CODEC =
            StreamCodec.of(SyncEntityPosPacket::encode, SyncEntityPosPacket::decode);

    public static SyncEntityPosPacket create(Entity entity, int segment) {
        return new SyncEntityPosPacket(
                entity.getId(),
                segment
        );
    }

    public static void encode(FriendlyByteBuf buf, SyncEntityPosPacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeInt(packet.segment);
    }

    public static SyncEntityPosPacket decode(FriendlyByteBuf buf) {
        int id = buf.readInt();
        int segment = buf.readInt();
        return new SyncEntityPosPacket(id, segment);
    }


    public static void handle(SyncEntityPosPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientTravelPathMover.updateSegment(packet.entityId, packet.segment);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
