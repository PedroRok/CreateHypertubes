package com.pedrorok.hypertube.managers;


import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 25/04/2025
 * @project Create Hypertube
 */
public class TravelData {

    private final List<Vec3> travelPoints;
    private final List<UUID> bezierConnections;
    private int travelIndex;

    public TravelData(BlockPos firstPipe, Level level, BlockPos entrancePos) {
        this.travelPoints = new ArrayList<>();
        this.bezierConnections = new ArrayList<>();
        travelPoints.add(entrancePos.getCenter().subtract(0, 0.2, 0));
        travelPoints.add(firstPipe.getCenter().subtract(0, 0.2, 0));

        addTravelPoint(firstPipe, level);
    }

    private void addTravelPoint(BlockPos pos, Level level) {
        BlockState blockState = level.getBlockState(pos);
        if (level.getBlockEntity(pos) instanceof HypertubeBlockEntity hypertubeBlockEntity
            && hypertubeBlockEntity.getConnection() != null
            && !bezierConnections.contains(hypertubeBlockEntity.getConnection().getUuid())) {
            travelPoints.addAll(hypertubeBlockEntity.getConnection().getBezierPoints());
            bezierConnections.add(hypertubeBlockEntity.getConnection().getUuid());
            addTravelPoint(hypertubeBlockEntity.getConnection().getToPos().pos(), level);
            return;
        }

        Block block = blockState.getBlock();
        if (!(block instanceof HypertubeBlock pipeBlock)) return;
        List<Direction> connectedFaces = pipeBlock.getConnectedFaces(blockState);
        for (Direction direction : connectedFaces) {
            BlockPos nextPipe = pos.relative(direction);
            if (travelPoints.contains(nextPipe)) continue;
            travelPoints.add(nextPipe.getCenter().subtract(0, 0.2, 0));
            addTravelPoint(nextPipe, level);
            break;
        }
    }

    public Vec3 getTravelPoint() {
        if (travelIndex >= travelPoints.size()) return null;
        return travelPoints.get(travelIndex);
    }

    public void getNextTravelPoint() {
        if (travelIndex >= travelPoints.size()) return;
        travelIndex++;
    }
}