package com.pedrorok.hypertube.core.travel;

import com.mojang.datafixers.util.Pair;
import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.HypertubeFunnelBlock;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeFunnelBlockEntity;
import com.pedrorok.hypertube.core.compat.Mods;
import com.pedrorok.hypertube.core.compat.sable.SableCompat;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import com.pedrorok.hypertube.blocks.blockentities.ActionTubeBlockEntity;
import com.pedrorok.hypertube.network.packets.ItemTravelFinishPacket;
import com.pedrorok.hypertube.network.packets.ItemTravelPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class ItemTravelManager {

    private static final Map<UUID, TravelingItem> travelingItems = new Object2ObjectArrayMap<>();

    public static void startItemTravel(ItemEntity itemEntity, BlockEntity entrance, float speed) {
        if (travelingItems.containsKey(itemEntity.getUUID())) return;

        BlockState state = entrance.getBlockState();
        BlockPos pos = entrance.getBlockPos();

        BlockPos relative = pos.relative(state.getValue(HyperEntranceBlock.FACING));
        TravelPathData travelPathData = new TravelPathData(relative, entrance.getLevel(), pos);

        if (travelPathData.getTravelPoints().size() < 3) {
            return;
        }

        float finalSpeed = speed * TravelConstants.DEFAULT_SPEED_MULTIPLIER;

        ItemStack itemStack = itemEntity.getItem().copy();
        UUID itemUuid = itemEntity.getUUID();
        itemEntity.discard();

        TravelingItem travelingItem = new TravelingItem(
                itemUuid,
                itemStack,
                entrance,
                itemEntity.position(),
                travelPathData.getTravelPoints(),
                travelPathData.getActionPoints(),
                finalSpeed,
                travelPathData.getEndDirection(entrance.getLevel()),
                travelPathData.getLastBlockPos()
        );

        travelingItems.put(itemUuid, travelingItem);

        ItemTravelPacket packet = new ItemTravelPacket(
                itemUuid,
                itemStack,
                travelPathData.getTravelPoints(),
                travelPathData.getActionPoints(),
                finalSpeed
        );
        PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) entrance.getLevel(),
                entrance.getLevel().getChunkAt(pos).getPos(), packet);

        HypertubeMod.LOGGER.debug("Item travel started: {} with speed {}", itemStack, finalSpeed);
    }

    public static void tick() {
        Iterator<Map.Entry<UUID, TravelingItem>> it = travelingItems.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, TravelingItem> entry = it.next();
            TravelingItem item = entry.getValue();
            if (item.tick()) {
                it.remove();
            }
        }
    }

    public static boolean isItemTraveling(UUID uuid) {
        return travelingItems.containsKey(uuid);
    }

    public static void removeItem(UUID uuid) {
        travelingItems.remove(uuid);
    }

    public static Collection<TravelingItem> getTravelingItems() {
        return travelingItems.values();
    }

    public static class TravelingItem {
        private final UUID uuid;
        private final ItemStack itemStack;
        private final List<Vec3> pathPoints;
        private final Set<BlockPos> actionPoints;
        private final Set<BlockPos> activeActionPoints;
        private float travelSpeed;
        private Vec3 lastDirection;
        private final BlockPos lastPos;
        private final Level level;

        private int currentSegment = 0;
        private Vec3 currentStart;
        private Vec3 currentEnd;
        private double totalDistance;
        private double traveled;

        private Vec3 currentPosition;

        public TravelingItem(UUID uuid, ItemStack itemStack, BlockEntity entrance, Vec3 entityPos,
                             List<Vec3> points, Set<BlockPos> actionPoints, float travelSpeed,
                             Vec3 lastDirection, BlockPos lastPos) {
            this.uuid = uuid;
            this.itemStack = itemStack;
            this.pathPoints = new ArrayList<>(points);
            this.actionPoints = new HashSet<>(actionPoints);
            this.activeActionPoints = new HashSet<>() {{
                add(entrance.getBlockPos());
            }};
            this.travelSpeed = travelSpeed;
            this.lastDirection = lastDirection;
            this.lastPos = lastPos;
            this.level = entrance.getLevel();

            Vec3 entrancePos = entrance.getBlockPos().getCenter();
            Vec3 entranceOffset = entityPos.subtract(Mods.SABLE.executeIfInstalled(() -> (pos) -> SableCompat.transformToWorld(level, pos), entrancePos));
            entranceOffset = Mods.SABLE.executeIfInstalled(() -> (dir) -> SableCompat.transformToSubLevel(level, entrancePos, Vec3.ZERO, dir).getSecond(), entranceOffset.normalize()).scale(entranceOffset.length());

            this.currentStart = entrancePos.add(entranceOffset);
            this.currentEnd = pathPoints.getFirst();
            this.totalDistance = currentStart.distanceTo(currentEnd);
            this.traveled = 0;
            this.currentPosition = this.currentStart;

            if (this.lastDirection == null) {
                this.lastDirection = pathPoints.getLast().subtract(pathPoints.get(pathPoints.size() - 2)).normalize();
            }
            this.pathPoints.add(this.pathPoints.getLast().add(this.lastDirection.scale(1)));
        }

        public boolean tick() {
            if (level == null || level.isClientSide) return true;

            if (traveled >= totalDistance) {
                currentSegment++;
                if (currentSegment >= pathPoints.size()) {
                    ejectItem();
                    return true;
                }
                currentStart = currentEnd;
                currentEnd = pathPoints.get(currentSegment);
                totalDistance = currentStart.distanceTo(currentEnd);
                traveled = 0;
            }

            if (!activeActionPoints.isEmpty()) {
                BlockPos actionPos = activeActionPoints.iterator().next();
                activeActionPoints.remove(actionPos);
                Block block = level.getBlockState(actionPos).getBlock();
                if (block instanceof ITubeActionPoint travelAction) {
                    travelAction.handleItemTravel(this, level, actionPos);
                }
                BlockEntity be = level.getBlockEntity(actionPos);
                if (be instanceof ActionTubeBlockEntity actionTubeBlockEntity && actionTubeBlockEntity.hasAnyTubeAttachment()) {
                    actionTubeBlockEntity.activateAllTubeAttachmentsForItem(this, actionPos);
                }
            }

            traveled += travelSpeed;
            Vec3 direction = currentEnd.subtract(currentStart).normalize();
            Vec3 newPos = currentStart.add(direction.scale(traveled));
            newPos = Mods.SABLE.executeIfInstalled(() -> (pos) -> SableCompat.transformToWorld(level, pos), newPos);

            currentPosition = newPos;

            return false;
        }

        private void ejectItem() {
            Vec3 lastDir = lastDirection;

            Vec3 lastBlockPos = lastPos.getCenter();
            BlockState blockState = level.getBlockState(lastPos);

            ItemStack remainingStack = itemStack.copy();

            if (blockState.getBlock() instanceof HypertubeFunnelBlock) {
                boolean passesFilter = true;
                BlockEntity be = level.getBlockEntity(lastPos);
                if (be instanceof HypertubeFunnelBlockEntity funnelBe) {
                    com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour filtering = funnelBe.getFiltering();
                    if (filtering != null && !filtering.test(remainingStack)) {
                        passesFilter = false;
                    }
                }
                if (passesFilter) {
                    Direction facing = blockState.getValue(HypertubeFunnelBlock.FACING);
                    remainingStack = tryInsertIntoAdjacentHandler(level, lastPos.relative(facing.getOpposite()),
                            facing, remainingStack);
                    if (!remainingStack.isEmpty()) {
                        remainingStack = tryInsertIntoAdjacentHandler(level, lastPos.below(),
                                Direction.UP, remainingStack);
                    }
                }
            }

            if (remainingStack.isEmpty()) {
                ItemTravelFinishPacket finishPacket = new ItemTravelFinishPacket(uuid);
                PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level,
                        level.getChunkAt(lastPos).getPos(), finishPacket);
                return;
            }

            if (blockState.getBlock() instanceof HyperEntranceBlock) {
                lastBlockPos = lastPos.relative(blockState.getValue(HyperEntranceBlock.FACING).getOpposite()).getCenter();
            } else if (blockState.getBlock() instanceof HypertubeFunnelBlock) {
                lastBlockPos = lastPos.relative(blockState.getValue(HypertubeFunnelBlock.FACING).getOpposite()).getCenter();
            }

            Pair<Vec3, Vec3> lastPosDir = Pair.of(lastBlockPos, lastDir);
            lastPosDir = Mods.SABLE.executeIfInstalled(() -> (posDir) -> SableCompat.transformToWorld(level, posDir.getFirst(), posDir.getSecond()), lastPosDir);
            lastBlockPos = lastPosDir.getFirst();
            lastDir = lastPosDir.getSecond();
            lastBlockPos = lastBlockPos.add(lastDir.scale(0.5));

            ItemEntity itemEntity = new ItemEntity(level, lastBlockPos.x, lastBlockPos.y, lastBlockPos.z, remainingStack);
            itemEntity.setDeltaMovement(lastDir.scale(Math.max(travelSpeed, 1f)));
            level.addFreshEntity(itemEntity);

            ItemTravelFinishPacket finishPacket = new ItemTravelFinishPacket(uuid);
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level,
                    level.getChunkAt(BlockPos.containing(lastBlockPos)).getPos(), finishPacket);
        }

        private ItemStack tryInsertIntoAdjacentHandler(Level level, BlockPos targetPos, Direction side, ItemStack stack) {
            if (stack.isEmpty()) {
                return stack;
            }

            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, side);
            if (handler == null) {
                return stack;
            }

            return ItemHandlerHelper.insertItemStacked(handler, stack, false);
        }

        public UUID getUuid() {
            return uuid;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public Vec3 getCurrentPosition() {
            return currentPosition;
        }

        public float getTravelSpeed() {
            return travelSpeed;
        }

        public void setTravelSpeed(float speed) {
            this.travelSpeed = speed;
        }
    }
}
