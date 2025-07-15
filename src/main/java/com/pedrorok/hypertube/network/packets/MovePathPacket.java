package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.core.travel.ClientTravelPathMover;
import com.pedrorok.hypertube.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
public record MovePathPacket(int entityId, List<Vec3> pathPoints,
                             double blocksPerSecond) implements Packet<MovePathPacket> {

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(pathPoints.size());
        for (Vec3 vec : pathPoints) {
            buf.writeDouble(vec.x);
            buf.writeDouble(vec.y);
            buf.writeDouble(vec.z);
        }
        buf.writeDouble(blocksPerSecond);
    }

    public MovePathPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), readPathPoints(buf), buf.readDouble());
    }

    private static List<Vec3> readPathPoints(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<Vec3> points = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            points.add(new Vec3(x, y, z));
        }
        return points;
    }


    @Override
    public void execute(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientTravelPathMover.startMoving(this);
        });
        ctx.get().setPacketHandled(true);
    }
}
