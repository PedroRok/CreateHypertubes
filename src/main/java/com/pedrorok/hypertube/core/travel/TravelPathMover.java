package com.pedrorok.hypertube.core.travel;

import com.pedrorok.hypertube.network.NetworkHandler;
import com.pedrorok.hypertube.network.packets.EntityTravelDirDataPacket;
import com.pedrorok.hypertube.network.packets.SyncEntityPosPacket;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
public class TravelPathMover {
    private final List<Vec3> pathPoints;
    @Getter
    private final float travelSpeed;
    private final BiConsumer<LivingEntity, Boolean> onFinishCallback;
    @Getter
    private final BlockPos lastPos;

    private int currentSegment = 0;
    private Vec3 currentStart;
    private Vec3 currentEnd;
    private double totalDistance;
    private double traveled;

    private Vec3 lastDirection = Vec3.ZERO;

    private final LivingEntity entity;

    public TravelPathMover(LivingEntity entity, List<Vec3> points, float travelSpeed, BlockPos lastPos, BiConsumer<LivingEntity, Boolean> onFinishCallback) {
        this.entity = entity;
        this.pathPoints = points;
        this.travelSpeed = travelSpeed;
        this.lastPos = lastPos;

        this.currentStart = entity.position();
        this.currentEnd = pathPoints.get(0).subtract(0, 0.25, 0);
        this.totalDistance = currentStart.distanceTo(currentEnd);
        this.traveled = 0;
        this.lastDirection = pathPoints.get(pathPoints.size() - 1).subtract(pathPoints.get(pathPoints.size() - 2)).normalize();

        this.onFinishCallback = onFinishCallback;
    }

    public void tickEntity(LivingEntity entity) {
        if (entity.isSpectator() || !entity.isAlive()) {
            onFinishCallback.accept(entity, true);
            return;
        }

        if (traveled >= totalDistance) {
            currentSegment++;
            if (currentSegment >= pathPoints.size()) {
                onFinishCallback.accept(entity, false);
                return;
            }
            currentStart = currentEnd;
            currentEnd = pathPoints.get(currentSegment).subtract(0, 0.25, 0);
            totalDistance = currentStart.distanceTo(currentEnd);
            traveled = 0;
        }

        Vec3 direction = currentEnd.subtract(currentStart).normalize().scale(travelSpeed);
        Vec3 newPos = entity.position().add(direction);

        entity.moveTo(newPos.x, newPos.y, newPos.z);
        traveled += travelSpeed;

        handleEntityDirection(entity, direction);
        if (entity instanceof Player) return;
        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                SyncEntityPosPacket.create(entity, currentSegment)
        );
    }


    private static void handleEntityDirection(LivingEntity entity, Vec3 direction) {
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        if (entity.level().isClientSide) return;
        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), EntityTravelDirDataPacket.create(entity));
    }

    public Vec3 getLastDir() {
        return lastDirection;
    }

    public void setClientFinish() {
        onFinishCallback.accept(entity, false);
    }
}
