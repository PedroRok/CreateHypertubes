package com.pedrorok.hypertube.core.travel;

import com.mojang.datafixers.util.Pair;
import com.pedrorok.hypertube.core.camera.DetachedPlayerDirController;
import com.pedrorok.hypertube.core.compat.Mods;
import com.pedrorok.hypertube.core.compat.sable.SableCompat;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import com.pedrorok.hypertube.network.packets.ActionPointReachPacket;
import com.pedrorok.hypertube.network.packets.FinishPathPacket;
import com.pedrorok.hypertube.network.packets.MovePathPacket;
import com.pedrorok.hypertube.network.packets.SpeedChangePacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
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

import java.util.*;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class ClientTravelPathMover {
    private static final Int2ObjectArrayMap<PathData> ACTIVE_PATHS = new Int2ObjectArrayMap<>();

    public static void startMoving(MovePathPacket packet) {
        final Minecraft mc = Minecraft.getInstance();
        final int id = packet.entityId();
        final Entity entity = mc.level.getEntity(id);
        final boolean isPlayer = mc.player.getId() == id;

        Mods.SABLE.executeIfInstalled(() -> () -> SableCompat.stickToSubLevel(entity, packet.actionPoints().iterator().next().getCenter()));
        ACTIVE_PATHS.put(packet.entityId(), new PathData(entity.position(), packet.pathPoints(), packet.actionPoints(), packet.travelSpeed(), isPlayer));
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
            if (!data.doClientTick(entity)) {
                it.remove();
                continue;
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

            data.doRenderTick(level.getEntity(id), partialTicks);
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
        }
    }

    public static PathData getData(int entityId) {
        return ACTIVE_PATHS.get(entityId);
    }

    public static class PathData {
        private final List<Vec3> pathPoints;
        private final Set<BlockPos> actionPoints;
        private double travelSpeed;

        private int currentIndex = 0;
        private int lastUpdateTick = 0;
        private Vec3 currentStart;
        private Vec3 currentEnd;
        private double totalDistance;
        private double traveled;
        private float previousPitch = 0;

        private boolean finished = false;

        @Getter
        private boolean clientPlayer = false;

        public PathData(Vec3 entityPos, List<Vec3> points, Set<BlockPos> actionPoints, double travelSpeed, boolean clientPlayer) {
            this.pathPoints = points;
            this.actionPoints = actionPoints;
            this.travelSpeed = travelSpeed;
            this.clientPlayer = clientPlayer;
            
            final Vec3 entrancePos = pathPoints.getFirst();
            Vec3 entranceOffset = Mods.SABLE.executeIfInstalled(() -> (pos) -> SableCompat.Client.transformToSubLevel(entrancePos, pos), entityPos).subtract(entrancePos);

            this.currentStart = entrancePos.add(entranceOffset);
            this.currentEnd = getCurrentTarget();

            this.totalDistance = currentStart.distanceTo(currentEnd);
            this.traveled = 0;
        }

        public Vec3 getCurrentTarget() {
            return pathPoints.get(Math.min(currentIndex, pathPoints.size() - 1));
        }

        private Vec3 getCurrentDirection() {
            if (currentEnd.equals(currentStart)) {
                return Vec3.ZERO;
            }
            return currentEnd.subtract(currentStart).normalize();
        }

        private void moveEntity(Entity entity, Vec3 pos) {
            entity.moveTo(pos.x, pos.y - 0.25, pos.z);
            entity.setDeltaMovement(Vec3.ZERO);
        }

        public boolean doClientTick(Entity entity) {
            if (entity == null || entity.isSpectator() || !entity.isAlive()) {
                return false;
            }

            if (traveled >= totalDistance) {
                currentIndex++;
                if (currentIndex >= pathPoints.size()) {
                    finished = true;
                } else {
                    currentStart = currentEnd;
                    currentEnd = getCurrentTarget();
                    totalDistance = currentStart.distanceTo(currentEnd);
                    traveled = 0;
                }
            }

            if (finished) {
                PacketDistributor.sendToServer(new FinishPathPacket(entity.getUUID()));
                Mods.SABLE.executeIfInstalled(() -> () -> SableCompat.stickToSubLevel(entity, null));
                return false;
            }

            traveled += travelSpeed;
            Vec3 direction = getCurrentDirection();
            Pair<Vec3, Vec3> newPosDir = Pair.of(currentStart.add(direction.scale(traveled)), direction);
            newPosDir = Mods.SABLE.executeIfInstalled(() -> (posDir) -> SableCompat.Client.transformToWorld(posDir.getFirst(), posDir.getSecond()), newPosDir);
            Vec3 newPos = newPosDir.getFirst();
            direction = newPosDir.getSecond();

            moveEntity(entity, newPos);
            if (clientPlayer)
                handleEntityDirection(direction);
            
            return true;
        }
        
        public void doRenderTick(Entity entity, float partialTicks) {
            if (finished || entity == null || entity.isSpectator() || !entity.isAlive()) {
                return;
            }
            handleActionPoint(entity);
            
            Vec3 direction = getCurrentDirection();
            Vec3 lastClientTickPos = currentStart.add(direction.scale(traveled));
            Vec3 nextClientTickPos = currentStart.add(direction.scale(traveled + travelSpeed));
            Vec3 endPos = Mods.SABLE.executeIfInstalled(() -> (pos) -> SableCompat.Client.transformToWorld(pos, true), lastClientTickPos)
                .lerp(
                    Mods.SABLE.executeIfInstalled(() -> (pos) -> SableCompat.Client.transformToWorld(pos, false), nextClientTickPos),
                    partialTicks
                );
            moveEntity(entity, endPos);
        }

        private static void handleEntityDirection(Vec3 direction) {
            float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
            float pitch = (float) Math.toDegrees(Math.atan2(-direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));
            DetachedPlayerDirController.get().setDetached(true);
            DetachedPlayerDirController.get().updateRotation(yaw, pitch);
        }

        public void handleActionPoint(Entity entity) {
            BlockPos entityPos = entity.getOnPos();
            if (!actionPoints.contains(entityPos)) return;
            actionPoints.remove(entityPos);
            BlockPos actionPos = entity.getOnPos();
            Block block = entity.level().getBlockState(actionPos).getBlock();
            if (block instanceof ITubeActionPoint travelAction) {
                PacketDistributor.sendToServer(new ActionPointReachPacket(entity.getUUID(), actionPos));
            }
        }

        public float getPitch() {
            Vec3 direction = Mods.SABLE.executeIfInstalled(() -> (dir) -> SableCompat.Client.transformToWorld(currentEnd, dir).getSecond(), getCurrentDirection());
            if (direction.equals(Vec3.ZERO) && previousPitch != -1) return previousPitch;
            previousPitch = (float) Math.toDegrees(Math.atan2(-direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z)));
            return previousPitch;
        }
    }
}