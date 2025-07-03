package com.pedrorok.hypertube.core.travel;

import com.pedrorok.hypertube.network.packets.PlayerTravelDirDataPacket;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

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
    private final BlockPos lastBlockPos;

    private int currentSegment = 0;
    private Vec3 currentStart;
    private Vec3 currentEnd;
    private double totalDistance;
    private double traveled;

    public boolean isFinished = false;

    private Vec3 lastDirection = Vec3.ZERO;

    public TravelPathMover(Vec3 entityPos, List<Vec3> points, float travelSpeed, BlockPos lastBlockPos, BiConsumer<LivingEntity, Boolean> onFinishCallback) {
        this.pathPoints = points;
        this.travelSpeed = travelSpeed / 20f;
        this.lastBlockPos = lastBlockPos;

        this.currentStart = entityPos;
        this.currentEnd = pathPoints.getFirst().subtract(0,0.25,0);
        this.totalDistance = currentStart.distanceTo(currentEnd);
        this.traveled = 0;

        this.onFinishCallback = onFinishCallback;
    }

    public void tickPlayer(LivingEntity entity) {
        if (entity.isSpectator() || !entity.isAlive()) {
            isFinished = true;
            onFinishCallback.accept(entity, true);
            return;
        }

        if (traveled >= totalDistance) {
            currentSegment++;
            if (currentSegment >= pathPoints.size()) {
                isFinished = true;
                onFinishCallback.accept(entity, false);
                return;
            }
            currentStart = entity.position();
            currentEnd = pathPoints.get(currentSegment).subtract(0,0.25,0);
            totalDistance = currentStart.distanceTo(currentEnd);
            traveled = 0;
            lastDirection = currentEnd.subtract(currentStart);
        }

        Vec3 direction = currentEnd.subtract(currentStart).normalize().scale(travelSpeed);
        Vec3 newPos = entity.position().add(direction);

        entity.setPos(newPos.x, newPos.y, newPos.z);
        traveled += travelSpeed;

        Vec3 previewPoint = getPreviewPoint(2);
        if (previewPoint == null) return;
        handleEntityDirection(entity, previewPoint.subtract(currentStart));
    }


    private static void handleEntityDirection(LivingEntity entity, Vec3 direction) {
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));
        System.out.println(direction);
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        if (entity instanceof Player player) {
            PacketDistributor.sendToPlayer((ServerPlayer) player, new PlayerTravelDirDataPacket(yaw, pitch));
        }
    }

    public Vec3 getLastDir() {
        return lastDirection;
    }

    public Vec3 getPreviewPoint(int offset) {
        if (currentSegment + offset >= pathPoints.size()) {
            return null;
        }
        return pathPoints.get(currentSegment + offset).subtract(0,0.25,0);
    }
}
