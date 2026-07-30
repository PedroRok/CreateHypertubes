package com.pedrorok.hypertube.core.travel.client;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.camera.DetachedPlayerDirController;
import com.pedrorok.hypertube.core.compat.Mods;
import com.pedrorok.hypertube.core.compat.sable.SableCompat;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.network.packets.ActionPointReachPacket;
import com.pedrorok.hypertube.network.packets.FinishPathPacket;
import com.pedrorok.hypertube.network.packets.MovePathPacket;
import com.pedrorok.hypertube.network.packets.SpeedChangePacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import lombok.Getter;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import com.pedrorok.hypertube.network.NetworkHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
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

    public static void stopMoving(int entityId) {
        PathData data = ACTIVE_PATHS.remove(entityId);
        if (data == null) return;
        // the server owns the end of the travel, so this is the usual way a path stops: release everything the
        // path was holding, the same way the client side finish does
        releasePathHolds(data, Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.getEntity(entityId));
    }

    private static void releasePathHolds(PathData data, @Nullable Entity entity) {
        if (entity != null) {
            Mods.SABLE.executeIfInstalled(() -> () -> SableCompat.stickToSubLevel(entity, null));
        }
        if (!data.isClientPlayer()) return;
        // give the mouse back as soon as the travel is over, the camera keeps easing out on its own
        DetachedPlayerDirController.get().setDetached(false);
    }

    public static void updateEntitySpeed(SpeedChangePacket packet) {
        PathData data = ACTIVE_PATHS.get(packet.entityId());
        if (data != null) {
            data.travelSpeed = packet.newSpeed();
        }
    }

    public static void onClientTick() {
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
                HypertubeMod.LOGGER.debug("Entity {} is no longer valid, removing from active paths", id);
                it.remove();
                continue;
            }

            if (data.isDone() && !data.isJunctionEnd()) {
                NetworkHandler.INSTANCE.sendToServer(new FinishPathPacket(entity.getUUID()));
                releasePathHolds(data, entity);
                it.remove();
                HypertubeMod.LOGGER.debug("Entity {} has finished its path, removing from active paths", id);
                continue;
            }

            data.updateLogicalPosition();
            entity.setDeltaMovement(data.getCurrentVelocity());
            if (data.isClientPlayer()) {
                handleEntityDirection(data.getWorldDirection());
                ClientTravelPathRender.handleClientPlayer(data);
            }
        }
    }

    public static void onRenderTick(float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        float partialTicks = partialTick;

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
            data.syncServerSegment(segment);
        }
    }

    public static PathData getData(int entityId) {
        return ACTIVE_PATHS.get(entityId);
    }

    public static class PathData {
        private final List<Vec3> points;
        private final Set<BlockPos> actionPoints;
        private double travelSpeed;

        private final Vec3[] route;
        private final double[] cumulative;
        private final double totalLength;

        private double traveled;
        private double previousTraveled;
        private int cachedSegment = 0;

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

            this.route = new Vec3[points.size() + 1];
            double pathLength = 0;
            for (int i = 0; i < points.size(); i++) {
                route[i + 1] = points.get(i).subtract(0, TravelConstants.PATH_Y_OFFSET, 0);
                if (i > 0) pathLength += route[i].distanceTo(route[i + 1]);
            }

            Vec3 start = entity.position();
            if (!points.isEmpty()) {
                Vec3 entranceLogical = route[1];
                Vec3 entranceOffset = Mods.SABLE.executeIfInstalled(() -> (pos) -> SableCompat.Client.transformToSubLevel(entranceLogical, pos), entity.position()).subtract(entranceLogical);

                start = entranceLogical.add(entranceOffset);
                if (start.distanceToSqr(entranceLogical) > pathLength * pathLength) {
                    start = entranceLogical;
                }
            }
            route[0] = start;

            this.cumulative = new double[route.length];
            for (int i = 1; i < route.length; i++) {
                cumulative[i] = cumulative[i - 1] + route[i - 1].distanceTo(route[i]);
            }
            this.totalLength = cumulative[cumulative.length - 1];

            this.currentLogicalPos = start;
            this.previousLogicalPos = start;
        }

        public boolean isDone() {
            return points.isEmpty() || traveled >= totalLength;
        }

        public int getCurrentIndex() {
            return isDone() ? points.size() : segmentAt(traveled);
        }

        public Vec3 getCurrentTarget() {
            int index = getCurrentIndex();
            if (index < points.size()) {
                return route[index + 1];
            }
            return currentLogicalPos;
        }

        public void updateLogicalPosition() {
            if (isDone()) return;

            previousTraveled = traveled;
            previousLogicalPos = currentLogicalPos;

            traveled = Math.min(totalLength, traveled + travelSpeed);
            currentLogicalPos = pointAt(traveled);
        }

        public void syncServerSegment(int segment) {
            if (segment < 0 || segment >= points.size() || isDone()) return;

            double segmentStart = cumulative[segment];
            double segmentEnd = cumulative[segment + 1];
            double tolerance = travelSpeed * (1 + latencyInTicks());
            if (traveled >= segmentStart - tolerance && traveled <= segmentEnd + tolerance) return;

            traveled = segmentStart;
            previousTraveled = segmentStart;
            currentLogicalPos = pointAt(segmentStart);
            previousLogicalPos = currentLogicalPos;
        }

        private static double latencyInTicks() {
            Minecraft mc = Minecraft.getInstance();
            ClientPacketListener connection = mc.getConnection();
            if (connection == null || mc.player == null) return 0;
            PlayerInfo playerInfo = connection.getPlayerInfo(mc.player.getUUID());
            if (playerInfo == null) return 0;
            return (double) playerInfo.getLatency() * SharedConstants.TICKS_PER_SECOND / 1000.0;
        }

        private Vec3 pointAt(double distance) {
            if (distance <= 0) return route[0];
            if (distance >= totalLength) return route[route.length - 1];

            int segment = segmentAt(distance);
            double segmentLength = cumulative[segment + 1] - cumulative[segment];
            if (segmentLength <= 0) return route[segment];
            return route[segment].lerp(route[segment + 1], (distance - cumulative[segment]) / segmentLength);
        }

        private int segmentAt(double distance) {
            if (route.length < 2) return 0;
            int segment = Mth.clamp(cachedSegment, 0, route.length - 2);
            while (segment > 0 && cumulative[segment] > distance) segment--;
            while (segment < route.length - 2 && cumulative[segment + 1] <= distance) segment++;
            cachedSegment = segment;
            return segment;
        }

        public float getPitch() {
            Vec3 dir = getWorldDirection();
            if (dir.equals(Vec3.ZERO) && previousPitch != -1) return previousPitch;
            float degrees = (float) Math.toDegrees(Math.atan2(-dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)));
            previousPitch = degrees;
            return degrees;
        }

        public void handleActionPoint(LivingEntity entity) {
            BlockPos actionPos = entity.getOnPos();
            if (!actionPoints.remove(actionPos)) return;
            Block block = entity.level().getBlockState(actionPos).getBlock();
            if (block instanceof ITubeActionPoint travelAction) {
                NetworkHandler.INSTANCE.sendToServer(new ActionPointReachPacket(entity.getUUID(), actionPos));
            }
        }

        public Vec3 getCurrentDirection() {
            if (currentLogicalPos.equals(previousLogicalPos)) {
                return Vec3.ZERO;
            }
            return currentLogicalPos.subtract(previousLogicalPos).normalize();
        }

        /**
         * The speed the entity is travelling at, as a vector. The position is forced every frame, so this is only
         * what vanilla carries over once the path ends: with a plain direction the entity would leave the tube
         * at one block per tick, throwing away the speed it was travelling at. The travel speed is used instead of
         * the distance moved this tick because the last step of a path is a partial one, which would otherwise
         * hand over an arbitrary fraction of the speed.
         */
        public Vec3 getCurrentVelocity() {
            return getWorldDirection().scale(travelSpeed);
        }

        public Vec3 getWorldDirection() {
            return Mods.SABLE.executeIfInstalled(() -> (dir) -> SableCompat.Client.transformToWorld(currentLogicalPos, dir).getSecond(), getCurrentDirection());
        }

        public Vec3 getRenderPosition(float partialTicks) {
            double renderDistance = Mth.lerp(Mth.clamp(partialTicks, 0f, 1f), previousTraveled, traveled);
            Vec3 logicalRender = pointAt(renderDistance);
            return Mods.SABLE.executeIfInstalled(() -> (pos) -> SableCompat.Client.transformToWorld(pos, true), logicalRender);
        }

        public BlockPos getLastBlockPos() {
            return BlockPos.containing(points.get(points.size() - 1));
        }
    }
}