package com.pedrorok.hypertube.core.travel.client;

import com.pedrorok.hypertube.blocks.blockentities.HyperJunctionBlockEntity;
import com.pedrorok.hypertube.core.camera.DetachedPlayerDirController;
import com.pedrorok.hypertube.core.compat.Mods;
import com.pedrorok.hypertube.core.compat.sable.SableCompat;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import com.pedrorok.hypertube.network.packets.*;
import com.pedrorok.hypertube.utils.JunctionDirectionUtils;
import com.pedrorok.hypertube.utils.MoveDirection;
import com.pedrorok.hypertube.utils.TubePulseRenderer;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class ClientTravelPathMover {
    private static final Int2ObjectArrayMap<PathData> ACTIVE_PATHS = new Int2ObjectArrayMap<>();

    public static void startMoving(MovePathPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        boolean isPlayer = mc.player.getId() == packet.entityId();
        Entity entity = mc.level.getEntity(packet.entityId());

        Mods.SABLE.executeIfInstalled(() -> () -> SableCompat.stickToSubLevel(entity, packet.actionPoints().iterator().next().getCenter()));
        boolean junctionEnd = packet.isJunctionEnd();
        PathData pathData = new PathData(entity,
                packet.pathPoints(),
                packet.actionPoints(),
                packet.travelSpeed(),
                isPlayer,
                junctionEnd,
                packet.junctionDirection());
        ACTIVE_PATHS.put(packet.entityId(), pathData);

        ClientTravelPathRender.handleStart(junctionEnd, pathData);
        if (!isPlayer || !junctionEnd) return;
        ClientKeyInputTracker.handlePlayerStart();
    }

    public static void updateEntitySpeed(SpeedChangePacket packet) {
        PathData data = ACTIVE_PATHS.get(packet.entityId());
        if (data != null) {
            data.travelSpeed = packet.newSpeed();
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) return;
        Level level = mc.level;
        if (level == null) return;

        Iterator<Map.Entry<Integer, PathData>> it = ACTIVE_PATHS.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            int id = entry.getKey();
            PathData data = entry.getValue();

            Entity entity = level.getEntity(id);
            if (entity == null || !entity.isAlive() || entity.isSpectator()) {
                it.remove();
                continue;
            }

            if (data.isDone()) {
                PacketDistributor.sendToServer(new FinishPathPacket(entity.getUUID()));
                Mods.SABLE.executeIfInstalled(() -> () -> SableCompat.stickToSubLevel(entity, null));
                it.remove();
                continue;
            }

            data.updateLogicalPosition();
            entity.setDeltaMovement(data.getCurrentDirection());
            if (data.isClientPlayer()) {
                handleEntityDirection(data.getWorldDirection());
                ClientTravelPathRender.handleClientPlayer(data);
            }
        }
    }




    @SubscribeEvent
    public static void onRenderTick(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        for (var entry : ACTIVE_PATHS.entrySet()) {
            int id = entry.getKey();
            PathData data = entry.getValue();

            Entity entity = level.getEntity(id);
            if (entity == null || !entity.isAlive() || entity.isSpectator()) continue;
            data.handleActionPoint((LivingEntity) entity);

            Vec3 renderPos = data.getRenderPosition(partialTicks);

            entity.moveTo(renderPos.x, renderPos.y, renderPos.z);
        }
    }

    private static void handleEntityDirection(Vec3 direction) {
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));
        DetachedPlayerDirController.get().setDetached(true);
        DetachedPlayerDirController.get().updateRotation(yaw, pitch);
    }

    public static void updateSegment(int entityId, int segment) {
        PathData data = ACTIVE_PATHS.get(entityId);
        if (data != null) {
            if (data.lastUpdateTick > 0) {
                data.lastUpdateTick--;
                return;
            }
            data.lastUpdateTick = 5;
            data.currentIndex = segment;
            data.updateLogicalPosition();
        }
    }

    public static PathData getData(int entityId) {
        return ACTIVE_PATHS.get(entityId);
    }

    public static class PathData {
        private final List<Vec3> points;
        private final Set<BlockPos> actionPoints;
        private double travelSpeed;
        @Getter
        private int currentIndex = 0;
        private int lastUpdateTick = 0;

        private Vec3 currentLogicalPos;
        private Vec3 previousLogicalPos;

        private float previousPitch = 0;

        @Getter
        private boolean clientPlayer;
        @Getter
        private boolean junctionEnd;
        @Getter
        @Nullable
        private Direction junctionDirection;

        public PathData(Entity entity, List<Vec3> points, Set<BlockPos> actionPoints, double blocksPerSecond, boolean clientPlayer, boolean isJunctionEnd, @Nullable Direction junctionDirection) {
            this.points = points;
            this.actionPoints = actionPoints;
            this.travelSpeed = blocksPerSecond;
            this.clientPlayer = clientPlayer;
            this.junctionEnd = isJunctionEnd;
            this.junctionDirection = junctionDirection;

            if (!points.isEmpty()) {
                Vec3 entranceLogical = points.get(0).subtract(0, 0.25, 0);
                Vec3 entranceOffset = Mods.SABLE.executeIfInstalled(() -> (pos) -> SableCompat.Client.transformToSubLevel(entranceLogical, pos), entity.position()).subtract(entranceLogical);

                this.currentLogicalPos = entranceLogical.add(entranceOffset);
                this.previousLogicalPos = this.currentLogicalPos;
            }
        }

        public boolean isDone() {
            return currentIndex >= points.size();
        }

        public Vec3 getCurrentTarget() {
            if (currentIndex < points.size()) {
                return points.get(currentIndex).subtract(0, 0.25, 0);
            }
            return currentLogicalPos;
        }

        public void updateLogicalPosition() {
            if (isDone()) return;

            Vec3 target = getCurrentTarget();
            double distanceToTarget = currentLogicalPos.distanceTo(target);
            boolean doHalfStep = true;
            previousLogicalPos = currentLogicalPos;
            if (distanceToTarget < travelSpeed) {
                currentLogicalPos = target;
                currentIndex = (int) (currentIndex + Math.max(1, travelSpeed));
                if (travelSpeed <= 1) {
                    doHalfStep = false;
                }
            }
            if (doHalfStep) {
                Vec3 direction = target.subtract(currentLogicalPos).normalize().scale(travelSpeed);
                currentLogicalPos = currentLogicalPos.add(direction);
            }
        }

        public float getPitch() {
            Vec3 dir = getWorldDirection();
            if (dir.equals(Vec3.ZERO) && previousPitch != -1) return previousPitch;
            float degrees = (float) Math.toDegrees(Math.atan2(-dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)));
            previousPitch = degrees;
            return degrees;
        }

        public void handleActionPoint(LivingEntity entity) {
            BlockPos entityPos = entity.getOnPos();
            if (!actionPoints.contains(entityPos)) return;
            actionPoints.remove(entityPos);
            BlockPos actionPos = entity.getOnPos();
            Block block = entity.level().getBlockState(actionPos).getBlock();
            if (block instanceof ITubeActionPoint travelAction) {
                PacketDistributor.sendToServer(new ActionPointReachPacket(entity.getUUID(), actionPos));
            }
        }

        public Vec3 getCurrentDirection() {
            if (currentLogicalPos.equals(previousLogicalPos)) {
                return Vec3.ZERO;
            }
            return currentLogicalPos.subtract(previousLogicalPos).normalize();
        }

        public Vec3 getWorldDirection() {
            return Mods.SABLE.executeIfInstalled(() -> (dir) -> SableCompat.Client.transformToWorld(currentLogicalPos, dir).getSecond(), getCurrentDirection());
        }

        public Vec3 getRenderPosition(float partialTicks) {
            Vec3 logicalRender = previousLogicalPos.lerp(currentLogicalPos, partialTicks);
            return Mods.SABLE.executeIfInstalled(() -> (pos) -> SableCompat.Client.transformToWorld(pos, true), logicalRender);
        }

        public BlockPos getLastBlockPos() {
            return BlockPos.containing(points.get(points.size() - 1));
        }
    }
}