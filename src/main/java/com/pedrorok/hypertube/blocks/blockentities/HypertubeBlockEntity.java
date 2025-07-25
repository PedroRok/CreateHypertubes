package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import com.pedrorok.hypertube.core.connection.TubeConnectionException;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnectionEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm. Created on 24/04/2025
 * @project Create Hypertube
 */
@Getter
public class HypertubeBlockEntity extends BlockEntity implements ITubeConnectionEntity {

    private IConnection connectionOne;
    private IConnection connectionTwo;

    public HypertubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // --------- Nbt Methods ---------
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        writeConnection(tag, new Tuple<>(connectionOne, "ConnectionTo"), new Tuple<>(connectionTwo, "ConnectionFrom"));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("ConnectionTo")) {
            this.connectionOne = getConnection(tag, "ConnectionTo");
        }
        if (tag.contains("ConnectionFrom")) {
            this.connectionTwo = getConnection(tag, "ConnectionFrom");
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @NotNull ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(@NotNull Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = pkt.getTag();
        loadAdditional(tag, registries);
    }
    // --------- Nbt Methods ---------

    // --------- Tube Segment Methods ---------
    public boolean wrenchClicked(Direction direction) {
        IConnection connectionInDirection = getConnectionInDirection(direction);
        if (connectionInDirection == null) {
            if (connectionOne != null) {
                connectionOne.updateTubeSegments(level);
            }
            if (connectionTwo != null) {
                connectionTwo.updateTubeSegments(level);
            }
            return true;
        }
        connectionInDirection.updateTubeSegments(level);
        return true;
    }
    // --------- Tube Segment Methods ---------

    @Override
    public List<Direction> getFacesConnectable() {
        if (connectionOne != null && connectionTwo != null) return List.of();

        List<Direction> possibleDirections = ((HypertubeBlock) getBlockState().getBlock()).getConnectedFaces(getBlockState());
        if (possibleDirections.isEmpty()) {
            return List.of(Direction.values());
        }

        possibleDirections.removeIf(direction -> {
            if (connectionOne != null) {
                return getConnectionDirection(direction, connectionOne);
            }
            if (connectionTwo != null) {
                return getConnectionDirection(direction, connectionTwo);
            }
            return false;
        });
        return possibleDirections;
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
    public @Nullable IConnection getConnectionInDirection(Direction direction) {
        if (getConnectionDirection(direction, connectionOne)) return connectionOne;
        if (getConnectionDirection(direction, connectionTwo)) return connectionTwo;
        return null;
    }

    private boolean getConnectionDirection(Direction direction, IConnection connection) {
        if (connection != null) {
            SimpleConnection sameConnectionBlockPos = IConnection.getSameConnectionBlockPos(connection, level, worldPosition);
            if (sameConnectionBlockPos != null) {
                Direction thisConn = sameConnectionBlockPos.direction();
                return thisConn != null && thisConn.equals(direction);
            }
        }
        return false;
    }

    @Override
    public @Nullable IConnection getThisConnectionFrom(SimpleConnection connection) {
        if (connectionOne instanceof BezierConnection bezierConnection) {
            if (connection.isSameConnection(bezierConnection.getFromPos()))
                return bezierConnection;
        }
        if (connectionTwo instanceof BezierConnection bezierConnection) {
            if (connection.isSameConnection(bezierConnection.getFromPos()))
                return bezierConnection;
        }
        return null;
    }

    @Override
    public boolean hasConnectionAvailable() {
        return connectionTwo == null || connectionOne == null;
    }

    public boolean isConnected() {
        return connectionOne != null || connectionTwo != null;
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

        if (level != null && !level.isClientSide()) {
            BlockState blockState = level.getBlockState(worldPosition);
            if (blockState.getBlock() instanceof HypertubeBlock hypertubeBlock) {
                hypertubeBlock.updateBlockStateFromEntity(blockState, level, worldPosition);
            }
        }
        setChanged();
        sync();
    }

    @Override
    public int blockBroken() {
        int toDrop = 0;
        if (connectionOne != null) {
            toDrop = blockBroken(level, connectionOne, worldPosition);
        }
        if (connectionTwo != null) {
            toDrop += blockBroken(level, connectionTwo, worldPosition);
        }
        return toDrop;
    }

    @Override
    public Vec3 getExitDirection() {
        if (connectionOne != null && connectionTwo != null) {
            return null;
        }
        if (connectionTwo != null) {
            return Vec3.atLowerCornerOf(IConnection.getSameConnectionBlockPos(connectionTwo, level, getBlockPos()).direction().getOpposite().getNormal());
        }
        if (connectionOne != null) {
            return Vec3.atLowerCornerOf(IConnection.getSameConnectionBlockPos(connectionOne, level, getBlockPos()).direction().getOpposite().getNormal());
        }
        return null;
    }

    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }
}