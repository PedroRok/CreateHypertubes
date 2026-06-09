package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HypertubeFunnelBlock;
import com.pedrorok.hypertube.config.ServerConfig;
import com.pedrorok.hypertube.core.connection.TubeConnectionException;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.travel.ItemTravelManager;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.utils.TubeUtils;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class HypertubeFunnelBlockEntity extends ActionTubeBlockEntity implements IHaveHoveringInformation {

    @Getter
    private IConnection connection;

    @Getter
    private FilteringBehaviour filtering;

    public HypertubeFunnelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        filtering = new FilteringBehaviour(this, new FunnelFilterSlotPositioning());
        behaviours.add(filtering);
        super.addBehaviours(behaviours);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("Connection")) {
            connection = getConnectionRelative(compound, "Connection", worldPosition);
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        writeConnectionRelative(compound, worldPosition, new Tuple<>(connection, "Connection"));
    }

    public boolean wrenchClicked(Direction direction) {
        IConnection connectionInDirection = getConnectionInDirection(direction);
        if (connectionInDirection == null) return false;
        connectionInDirection.updateTubeSegments(level);
        return true;
    }

    private int extractionCooldown = 0;

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) return;

        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        boolean isPowered = state.getValue(HypertubeFunnelBlock.POWERED);
        if (isPowered) return;

        float speed = this.getSpeed();
        if (Math.abs(speed) >= TravelConstants.NEEDED_SPEED) {
            if (extractionCooldown > 0) {
                extractionCooldown--;
            } else {
                if (tryExtractAndSendItem(state, pos, speed)) {
                    extractionCooldown = Math.max(5, (int) (160.0 / Math.abs(speed)));
                }
            }
        }
    }

    private boolean tryExtractAndSendItem(BlockState state, BlockPos pos, float speed) {
        if (connection == null) return false;

        Direction facing = state.getValue(HypertubeFunnelBlock.FACING);
        Direction opposite = facing.getOpposite();
        BlockPos chestPos = pos.relative(opposite);

        IItemHandler handler = level.getCapability(
                Capabilities.ItemHandler.BLOCK,
                chestPos,
                facing
        );

        if (handler != null) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stackInSlot = handler.getStackInSlot(slot);
                if (stackInSlot.isEmpty()) continue;
                if (filtering != null && !filtering.test(stackInSlot)) continue;

                ItemStack simulated = handler.extractItem(slot, 64, true);
                if (simulated.isEmpty()) continue;

                ItemStack extracted = handler.extractItem(slot, simulated.getCount(), false);
                if (!extracted.isEmpty()) {
                    sendItemIntoTube(extracted, state, pos, speed);
                    return true;
                }
            }
        }
        return false;
    }

    private void sendItemIntoTube(ItemStack stack, BlockState state, BlockPos pos, float speed) {
        Vec3 spawnPos = pos.getCenter();
        ItemEntity itemEntity = new ItemEntity(level, spawnPos.x, spawnPos.y, spawnPos.z, stack);
        itemEntity.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(itemEntity);
        ItemTravelManager.startItemTravel(itemEntity, this, TubeUtils.calculateTravelSpeed(Math.abs(speed)));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (getBlockState().getValue(HypertubeFunnelBlock.POWERED)) {
            tooltip.add(Component.literal("     ").append(Component.translatable("tooltip.create_hypertube.funnel_powered").withColor(0xFF0000)));
        }
        return true;
    }

    @Override
    public void setConnection(IConnection connection, Direction thisConnectionDir) {
        if (this.connection == null) {
            this.connection = connection;
        } else {
            HypertubeMod.LOGGER.error(new TubeConnectionException("Connection could not define connection", this.connection, connection).getMessage());
            return;
        }
        setChanged();
        sync();
    }

    @Override
    public void clearConnection(IConnection connection) {
        if (this.connection != null && this.connection.isSameConnection(connection)) {
            this.connection = null;
        } else {
            HypertubeMod.LOGGER.error(new TubeConnectionException("Connection could not be cleared", this.connection, connection).getMessage());
            return;
        }
        setChanged();
        sync();
    }

    @Override
    public List<Direction> getFacesConnectable() {
        if (connection != null) return List.of();
        return List.of(getBlockState().getValue(HypertubeFunnelBlock.FACING));
    }

    @Override
    public List<IConnection> getConnections() {
        List<IConnection> connections = new ArrayList<>();
        if (connection != null) {
            connections.add(connection);
        }
        return connections;
    }

    @Override
    public Vec3 getExitDirection() {
        if (getBlockState().hasProperty(HypertubeFunnelBlock.FACING)) {
            Direction facing = getBlockState().getValue(HypertubeFunnelBlock.FACING).getOpposite();
            return Vec3.atLowerCornerOf(facing.getNormal());
        }
        return null;
    }

    @Override
    protected int getConnectionCount() {
        return 1;
    }

    public float calculateStressApplied() {
        float impact = (float) ServerConfig.get().STRESS_IMPACT_ENTRANCE.getAsDouble();
        this.lastStressApplied = impact;
        return impact;
    }

    private final IItemHandler itemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            BlockState state = getBlockState();
            if (state.getValue(HypertubeFunnelBlock.POWERED)) {
                return stack;
            }
            if (connection == null) {
                return stack;
            }
            if (filtering != null && !filtering.test(stack)) {
                return stack;
            }

            if (!simulate) {
                float speed = Math.max(TravelConstants.NEEDED_SPEED, Math.abs(getSpeed()));
                sendItemIntoTube(stack.copy(), state, getBlockPos(), speed);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return filtering == null || filtering.test(stack);
        }
    };

    public IItemHandler getItemHandler(Direction side) {
        if (!getBlockState().hasProperty(HypertubeFunnelBlock.FACING)) return null;
        Direction facing = getBlockState().getValue(HypertubeFunnelBlock.FACING);
        if (side == null || side == facing.getOpposite()) {
            return itemHandler;
        }
        return null;
    }

    private static class FunnelFilterSlotPositioning extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return new Vec3(0.5, 0.75, 0.5);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getOpposite() == state.getValue(HypertubeFunnelBlock.FACING);
        }
    }
}
