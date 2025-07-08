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

            Vec3 currentPos = entity.position();
            Vec3 target = data.getCurrentTarget().subtract(0,0.25,0);

            double distance = currentPos.distanceTo(target);
            if (distance < data.blocksPerTick) {
                entity.setPos(target.x, target.y, target.z);
                data.advanceToNextPoint();
                continue;
            }

            Vec3 direction = target.subtract(currentPos).normalize().scale(data.blocksPerTick);
            Vec3 newPos = currentPos.add(direction);
            entity.setPos(newPos.x, newPos.y, newPos.z);
        }
    }

    private static class PathData {
        private final List<Vec3> points;
        private final double blocksPerTick;
        private int currentIndex = 0;

        public PathData(List<Vec3> points, double blocksPerSecond) {
            this.points = points;
            this.blocksPerTick = blocksPerSecond / 20.0;
        }

        public boolean isDone() {
            return currentIndex >= points.size();
        }

        public Vec3 getCurrentTarget() {
            return points.get(currentIndex);
        }

        public void advanceToNextPoint() {
            currentIndex++;
        }
    }
}
