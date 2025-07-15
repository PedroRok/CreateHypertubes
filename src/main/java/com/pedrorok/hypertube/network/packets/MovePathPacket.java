package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.travel.ClientTravelPathMover;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
public record MovePathPacket(int entityId, List<Vec3> pathPoints,
                             double blocksPerSecond) implements CustomPacketPayload {

    public static final Type<MovePathPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "entity_travel_path")
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
        buf.writeDouble(packet.blocksPerSecond);
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
        double speed = buf.readDouble();
        return new MovePathPacket(id, points, speed);
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
