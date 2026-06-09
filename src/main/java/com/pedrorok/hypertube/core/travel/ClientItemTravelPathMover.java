package com.pedrorok.hypertube.core.travel;

import com.pedrorok.hypertube.core.compat.Mods;
import com.pedrorok.hypertube.core.compat.sable.SableCompat;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import com.pedrorok.hypertube.network.packets.ActionPointReachPacket;
import com.pedrorok.hypertube.network.packets.ItemTravelPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
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

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientItemTravelPathMover {
    private static final Map<UUID, ItemPathData> ACTIVE_PATHS = new Object2ObjectArrayMap<>();

    public static void startMoving(ItemTravelPacket packet) {
        ACTIVE_PATHS.put(packet.itemUuid(), new ItemPathData(
                packet.itemStack(),
                packet.pathPoints(),
                packet.actionPoints(),
                packet.travelSpeed()
        ));
    }

    public static void finishTravel(UUID itemUuid) {
        ACTIVE_PATHS.remove(itemUuid);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) return;
        Level level = mc.level;
        if (level == null) return;

        Iterator<Map.Entry<UUID, ItemPathData>> it = ACTIVE_PATHS.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            ItemPathData data = entry.getValue();
            if (!data.doClientTick()) {
                it.remove();
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
            ItemPathData data = entry.getValue();
            data.doRenderTick(partialTicks);
        }
    }

    public static Collection<ItemPathData> getActivePaths() {
        return ACTIVE_PATHS.values();
    }

    public static class ItemPathData {
        private final ItemStack itemStack;
        private final List<Vec3> pathPoints;
        private final Set<BlockPos> actionPoints;
        private double travelSpeed;

        private int currentIndex = 0;
        private Vec3 currentStart;
        private Vec3 currentEnd;
        private double totalDistance;
        private double traveled;

        private boolean finished = false;
        private Vec3 currentPosition;
        private Vec3 renderPosition;

        public ItemPathData(ItemStack itemStack, List<Vec3> points, Set<BlockPos> actionPoints, double travelSpeed) {
            this.itemStack = itemStack;
            this.pathPoints = new ArrayList<>(points);
            this.actionPoints = new HashSet<>(actionPoints);
            this.travelSpeed = travelSpeed;

            this.currentStart = pathPoints.getFirst();
            this.currentEnd = pathPoints.get(Math.min(1, pathPoints.size() - 1));
            this.totalDistance = currentStart.distanceTo(currentEnd);
            this.traveled = 0;
            this.currentPosition = this.currentStart;
            this.renderPosition = this.currentStart;
        }

        public boolean doClientTick() {
            if (finished) return false;

            if (traveled >= totalDistance) {
                currentIndex++;
                if (currentIndex >= pathPoints.size()) {
                    finished = true;
                    return false;
                }
                currentStart = currentEnd;
                currentEnd = pathPoints.get(Math.min(currentIndex, pathPoints.size() - 1));
                totalDistance = currentStart.distanceTo(currentEnd);
                traveled = 0;
            }

            traveled += travelSpeed;
            Vec3 direction = currentEnd.subtract(currentStart).normalize();
            if (!direction.equals(Vec3.ZERO)) {
                Vec3 newPos = currentStart.add(direction.scale(traveled));
                newPos = Mods.SABLE.executeIfInstalled(() -> (pos) -> SableCompat.Client.transformToWorld(pos, true), newPos);
                currentPosition = newPos;
            }

            handleActionPoint();
            return true;
        }

        public void doRenderTick(float partialTicks) {
            if (finished) return;

            Vec3 direction = currentEnd.subtract(currentStart).normalize();
            if (direction.equals(Vec3.ZERO)) {
                renderPosition = currentPosition;
                return;
            }

            double renderTraveled = traveled + travelSpeed * partialTicks;
            Vec3 nextPos = currentStart.add(direction.scale(Math.min(renderTraveled, totalDistance)));
            nextPos = Mods.SABLE.executeIfInstalled(() -> (pos) -> SableCompat.Client.transformToWorld(pos, true), nextPos);

            renderPosition = currentPosition.lerp(nextPos, partialTicks);
        }

        private void handleActionPoint() {
            BlockPos entityPos = BlockPos.containing(currentPosition);
            if (!actionPoints.contains(entityPos)) return;
            actionPoints.remove(entityPos);
            Level level = Minecraft.getInstance().level;
            if (level == null) return;
            Block block = level.getBlockState(entityPos).getBlock();
            if (block instanceof ITubeActionPoint) {
                PacketDistributor.sendToServer(new ActionPointReachPacket(new UUID(0, 0), entityPos));
            }
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public Vec3 getRenderPosition() {
            return renderPosition;
        }

        public Vec3 getCurrentPosition() {
            return currentPosition;
        }

        public boolean isFinished() {
            return finished;
        }
    }
}