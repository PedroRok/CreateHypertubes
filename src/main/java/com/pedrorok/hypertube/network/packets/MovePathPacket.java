package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.core.travel.ClientTravelPathMover;
import net.minecraft.core.BlockPos;
import com.pedrorok.hypertube.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.Set;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
public record MovePathPacket(int entityId, List<Vec3> pathPoints, Set<BlockPos> actionPoints,
                             double travelSpeed) implements Packet<MovePathPacket> {

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(pathPoints.size());
        for (Vec3 vec : pathPoints) {
            buf.writeDouble(vec.x);
            buf.writeDouble(vec.y);
            buf.writeDouble(vec.z);
        }
        buf.writeInt(actionPoints.size());
        for (BlockPos blockPos : actionPoints) {
            buf.writeBlockPos(blockPos);
        }
        buf.writeDouble(travelSpeed);
    }

    public MovePathPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), readPathPoints(buf), buf.readDouble());
    }

    public static MovePathPacket decode(FriendlyByteBuf buf) {
        int id = buf.readInt();
        int size = buf.readInt();
        List<Vec3> points = new ArrayList<>(size);
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
        return new MovePathPacket(id, points, actionPoints, speed);
    }


    @Override
    public void execute(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientTravelPathMover.startMoving(this);
        });
        ctx.get().setPacketHandled(true);
    }
}
