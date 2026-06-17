package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.travel.client.ClientTravelPathMover;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
public record MovePathPacket(int entityId, List<Vec3> pathPoints, Set<BlockPos> actionPoints,
                             double travelSpeed, boolean isJunctionEnd,
                             @Nullable Direction junctionDirection) implements CustomPacketPayload {

    public static final Type<MovePathPacket> TYPE = new Type<>(
            HypertubeMod.of("entity_travel_path")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MovePathPacket> STREAM_CODEC =
            StreamCodec.of(MovePathPacket::encode, MovePathPacket::decode);


    public static void encode(FriendlyByteBuf buf, MovePathPacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeInt(packet.pathPoints.size());
        for (Vec3 vec : packet.pathPoints) {
            buf.writeDouble(vec.x);
            buf.writeDouble(vec.y);
            buf.writeDouble(vec.z);
        }
        buf.writeInt(packet.actionPoints.size());
        for (BlockPos blockPos : packet.actionPoints) {
            buf.writeBlockPos(blockPos);
        }
        buf.writeDouble(packet.travelSpeed);
        buf.writeBoolean(packet.isJunctionEnd);

        if (packet.isJunctionEnd)
            buf.writeEnum(packet.junctionDirection);
    }

    public static MovePathPacket decode(FriendlyByteBuf buf) {
        int id = buf.readInt();
        int size = buf.readInt();
        List<Vec3> points = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            points.add(new Vec3(x, y, z));
        }
        size = buf.readInt();
        Set<BlockPos> actionPoints = new HashSet<>();
        for (int i = 0; i < size; i++) {
            actionPoints.add(buf.readBlockPos());
        }
        double speed = buf.readDouble();
        boolean isJunctionEnd = buf.readBoolean();
        Direction junctionDirection = null;
        if (isJunctionEnd)
            junctionDirection = buf.readEnum(Direction.class);
        return new MovePathPacket(id, points, actionPoints, speed, isJunctionEnd, junctionDirection);
    }

    public static void handle(MovePathPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientTravelPathMover.startMoving(packet);
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
