package com.pedrorok.hypertube.core.travel;

import com.pedrorok.hypertube.core.camera.DetachedPlayerDirController;
import com.pedrorok.hypertube.network.packets.MovePathPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class ClientTravelPathMover {
    private static final Map<Integer, PathData> ACTIVE_PATHS = new HashMap<>();

    public static void startMoving(MovePathPacket packet) {
        ACTIVE_PATHS.put(packet.entityId(), new PathData(packet.pathPoints(), packet.blocksPerSecond()));
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
            if (entity == null || !entity.isAlive()) {
                it.remove();
                continue;
            }

            if (data.isDone()) {
                it.remove();
                continue;
            }

            data.updateLogicalPosition();
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
            if (entity == null || !entity.isAlive()) continue;

            Vec3 renderPos = data.getRenderPosition(partialTicks);

            entity.setPos(renderPos.x, renderPos.y, renderPos.z);
        }
    }

    private static class PathData {
        private final List<Vec3> points;
        private final double blocksPerTick;
        private int currentIndex = 0;

        private Vec3 currentLogicalPos;
        private Vec3 previousLogicalPos;

        public PathData(List<Vec3> points, double blocksPerSecond) {
            this.points = points;
            this.blocksPerTick = blocksPerSecond / 20.0;

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

        public Vec3 getRenderPosition(float partialTicks) {
            return previousLogicalPos.lerp(currentLogicalPos, partialTicks);
        }
    }
}