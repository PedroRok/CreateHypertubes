package com.pedrorok.hypertube.core.travel;

import com.pedrorok.hypertube.core.camera.DetachedPlayerDirController;
import com.pedrorok.hypertube.network.NetworkHandler;
import com.pedrorok.hypertube.network.packets.FinishPathPacket;
import com.pedrorok.hypertube.network.packets.MovePathPacket;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientTravelPathMover {
    private static final Map<Integer, PathData> ACTIVE_PATHS = new HashMap<>();

    public static void startMoving(MovePathPacket packet) {
        boolean isPlayer = Minecraft.getInstance().player.getId() == packet.entityId();
        ACTIVE_PATHS.put(packet.entityId(), new PathData(packet.pathPoints(), packet.blocksPerSecond(), isPlayer));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
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
            if (entity == null || !entity.isAlive()) {
                it.remove();
                continue;
            }

            if (data.isDone()) {
                it.remove();
                NetworkHandler.INSTANCE.send(PacketDistributor.SERVER.noArg(), new FinishPathPacket(entity.getUUID()));
                continue;
            }

            data.updateLogicalPosition();
            if (data.isClientPlayer())
                handleEntityDirection(data.getCurrentDirection());
        }
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        float partialTicks = event.renderTickTime;

        for (var entry : ACTIVE_PATHS.entrySet()) {
            int id = entry.getKey();
            PathData data = entry.getValue();
            if (data.isDone()) return;

            Entity entity = level.getEntity(id);
            if (entity == null || !entity.isAlive()) continue;

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
            //data.lastUpdateTick = 5;
            //data.currentIndex = segment;
            //data.updateLogicalPosition();
        }
    }

    private static class PathData {
        private final List<Vec3> points;
        private final double blocksPerTick;
        private int currentIndex = 0;
        private int lastUpdateTick = 0;

        private Vec3 currentLogicalPos;
        private Vec3 previousLogicalPos;

        @Getter
        private boolean clientPlayer;

        public PathData(List<Vec3> points, double blocksPerSecond, boolean clientPlayer) {
            this.points = points;
            this.blocksPerTick = blocksPerSecond / 20.0;
            this.clientPlayer = clientPlayer;

            if (!points.isEmpty()) {
                this.currentLogicalPos = points.get(0).subtract(0, 0.25, 0);
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

            if (distanceToTarget < blocksPerTick) {
                previousLogicalPos = currentLogicalPos;
                currentLogicalPos = target;
                currentIndex++;
            } else {
                previousLogicalPos = currentLogicalPos;
                Vec3 direction = target.subtract(currentLogicalPos).normalize().scale(blocksPerTick);
                currentLogicalPos = currentLogicalPos.add(direction);
            }
        }

        public Vec3 getCurrentDirection() {
            if (currentLogicalPos.equals(previousLogicalPos)) {
                return Vec3.ZERO;
            }
            return currentLogicalPos.subtract(previousLogicalPos).normalize();
        }

        public Vec3 getRenderPosition(float partialTicks) {
            return previousLogicalPos.lerp(currentLogicalPos, partialTicks);
        }
    }
}