package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HyperAcceleratorBlock;
import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.core.connection.TubeConnectionException;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.kinetics.base.IRotate;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class HyperAcceleratorBlockEntity extends ActionTubeBlockEntity implements IHaveHoveringInformation {

    private final UUID tubeSoundId = UUID.randomUUID();

    @Getter
    private IConnection connectionOne;
    @Getter
    private IConnection connectionTwo;

    public HyperAcceleratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // --------- Nbt Methods ---------
    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("ConnectionOne")) {
            connectionOne = getConnection(compound, "ConnectionOne");
        }
        if (compound.contains("ConnectionTwo")) {
            connectionTwo = getConnection(compound, "ConnectionTwo");
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        writeConnection(compound, new Tuple<>(connectionOne, "ConnectionOne"), new Tuple<>(connectionTwo, "ConnectionTwo"));
    }
    // --------- Nbt Methods ---------

    // --------- Tube Segment Methods ---------
    public boolean wrenchClicked(Direction direction) {
        IConnection connectionInDirection = getConnectionInDirection(direction);
        if (connectionInDirection == null) return false;
        connectionInDirection.updateTubeSegments(level);
        return true;
    }

    // --------- Tube Segment Methods ---------
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            tickClient();
            return;
        }

        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        float actualSpeed = Math.abs(this.getSpeed());
        Boolean isOpen = state.getValue(HyperAcceleratorBlock.OPEN);

        LivingEntity nearbyEntity = getNearbyLivingEntities((ServerLevel) level, pos.getCenter());

        boolean canOpen = nearbyEntity != null && nearbyEntity.getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG);

        isTubeClosed(canOpen, isOpen);
    }


    @OnlyIn(Dist.CLIENT)
    private void tickClient() {
        TubeSoundManager.TubeAmbientSound sound = TubeSoundManager.getAmbientSound(tubeSoundId);
        playClientEffects(sound);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        float finalSpeed = Math.abs(this.getSpeed());
        boolean hasNeededSpeed = finalSpeed < TravelConstants.NEEDED_SPEED;
        IRotate.SpeedLevel.getFormattedSpeedText(speed, hasNeededSpeed)
                .forGoggles(tooltip);

        if (hasNeededSpeed) {
            tooltip.add(Component.literal("     ")
                    .append(Component.literal("\u2592 "))
                    .append(Component.translatable("tooltip.create_hypertube.entrance_no_speed"))
                    .withColor(0xFF0000));
        }
        return true;
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return false;
    }

    @Override
    public void setConnection(IConnection connection, Direction thisConnectionDir) {
        if (connectionOne == null) {
            connectionOne = connection;
        } else if (connectionTwo == null) {
            connectionTwo = connection;
        } else {
            HypertubeMod.LOGGER.error(new TubeConnectionException("Connection could not define connection", connection, connectionOne, connectionTwo).getMessage());
            return;
        }
        if (level != null && !level.isClientSide()) {
            BlockState blockState = level.getBlockState(worldPosition);
            if (blockState.getBlock() instanceof HypertubeBlock hypertubeBlock) {
                hypertubeBlock.updateBlockStateFromEntity(blockState, level, worldPosition);
                if (thisConnectionDir != null) {
                    BlockState state = hypertubeBlock.getState(blockState, List.of(thisConnectionDir), true);
                    hypertubeBlock.updateBlockState(level, worldPosition, state);
                }
            }
        }
        setChanged();
        sync();
    }

    @Override
    public void clearConnection(IConnection connection) {
        if (connectionOne != null && connectionOne.isSameConnection(connection)) {
            connectionOne = null;
        } else if (connectionTwo != null && connectionTwo.isSameConnection(connection)) {
            connectionTwo = null;
        } else {
            HypertubeMod.LOGGER.error(new TubeConnectionException("Connection could not be cleared", connection, connectionOne, connectionTwo).getMessage());
            return;
        }
        setChanged();
        sync();
    }

    @Override
    public List<IConnection> getConnections() {
        List<IConnection> connections = new ArrayList<>();
        if (connectionOne != null) {
            connections.add(connectionOne);
        }
        if (connectionTwo != null) {
            connections.add(connectionTwo);
        }
        return connections;
    }

    @Override
    protected int getConnectionCount() {
        return 2;
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        level.setBlock(getBlockPos(), this.getBlockState().setValue(HyperAcceleratorBlock.ACTIVE, Math.abs(this.getSpeed()) >= TravelConstants.NEEDED_SPEED), 3);
    }
}
