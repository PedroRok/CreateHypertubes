package com.pedrorok.hypertube.core.collision;

import com.pedrorok.hypertube.blocks.TubePathBlock;
import com.pedrorok.hypertube.blocks.blockentities.TubePathBlockEntity;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import com.pedrorok.hypertube.core.connection.interfaces.IConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnectionEntity;
import com.pedrorok.hypertube.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm.
 * @project Create Hypertube
 */
public final class TubeFiller {

    private TubeFiller() {
    }

    public static void place(Level level, BezierConnection bezier) {
        if (level == null || level.isClientSide) return;

        BlockPos owner = bezier.getFromPos().pos();
        List<Vec3> points = bezier.getBezierPoints(level, owner);
        if (points.size() < 2) return;

        for (BlockPos pos : TubeCollision.occupiedVolume(points)) {
            if (!canReplace(level, pos)) continue;

            boolean waterlogged = level.getFluidState(pos).getType() == Fluids.WATER;
            BlockState fillerState = ModBlocks.TUBE_PATH.getDefaultState()
                    .setValue(TubePathBlock.WATERLOGGED, waterlogged);
            level.setBlock(pos, fillerState, Block.UPDATE_ALL);

            if (level.getBlockEntity(pos) instanceof TubePathBlockEntity filler) {
                filler.bind(owner, TubeCollision.boxes(points, pos));
            }
        }
    }

    public static void remove(Level level, BezierConnection bezier) {
        if (level == null || level.isClientSide) return;

        BlockPos owner = bezier.getFromPos().pos();
        List<Vec3> points = bezier.getBezierPoints(level, owner);
        if (points.size() < 2) return;

        for (BlockPos pos : TubeCollision.occupiedVolume(points)) {
            if (level.getBlockEntity(pos) instanceof TubePathBlockEntity filler
                    && owner.equals(filler.owner())) {
                BlockState state = level.getBlockState(pos);
                boolean waterlogged = state.hasProperty(TubePathBlock.WATERLOGGED)
                        && state.getValue(TubePathBlock.WATERLOGGED);
                level.setBlock(pos,
                        waterlogged ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL);
            }
        }
    }

    public static void removeAll(Level level, BezierConnection bezier) {
        if (level == null || level.isClientSide) return;

        remove(level, bezier);

        BlockPos fromPos = bezier.getFromPos().pos();
        if (level.getBlockEntity(fromPos) instanceof ITubeConnectionEntity fromTube) {
            fromTube.clearConnection(bezier);
        }
        SimpleConnection to = bezier.getToPos();
        if (to != null && level.getBlockEntity(to.pos()) instanceof ITubeConnectionEntity toTube) {
            toTube.clearConnection(bezier.getFromPos());
        }
    }

    public static @Nullable BezierConnection ownerBezier(Level level, BlockPos fillerPos) {
        if (!(level.getBlockEntity(fillerPos) instanceof TubePathBlockEntity filler)) return null;
        BlockPos ownerPos = filler.owner();
        if (ownerPos == null || !(level.getBlockEntity(ownerPos) instanceof ITubeConnectionEntity owner)) {
            return null;
        }
        BezierConnection fallback = null;
        for (IConnection connection : owner.getConnections()) {
            BezierConnection bezier = connection.getThisEntranceConnection(level);
            if (bezier == null || !bezier.getFromPos().pos().equals(ownerPos)) {
                continue;
            }
            fallback = bezier;
            if (TubeCollision.occupiedVolume(bezier.getBezierPoints(level, ownerPos)).contains(fillerPos)) {
                return bezier;
            }
        }
        return fallback;
    }

    private static boolean canReplace(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof TubePathBlock) return false;
        return state.isAir() || state.canBeReplaced();
    }
}
