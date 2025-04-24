package com.pedrorok.hypertube.managers.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.hypertube.blocks.HypertubeBaseBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.items.HypertubeItem;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModDataComponent;
import com.pedrorok.hypertube.registry.ModItems;
import com.simibubi.create.content.trains.track.*;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * @author Rok, Pedro Lucas nmm. Created on 23/04/2025
 * @project Create Hypertube
 */
public class TubePlacement {

    static BlockPos hoveringPos;
    static LerpedFloat animation = LerpedFloat.linear()
            .startWithValue(0);




    /*public static PlacementInfo tryConnect(Level level, Player player, BlockPos pos2, BlockState state2,
                                                          ItemStack stack, boolean girder, boolean maximiseTurn) {
        Vec3 lookVec = player.getLookAngle();
        int lookAngle = (int) (22.5 + AngleHelper.deg(Mth.atan2(lookVec.z, lookVec.x)) % 360) / 8;
        int maxLength = AllConfigs.server().trains.maxTrackPlacementLength.get();

        if (level.isClientSide && cached != null && pos2.equals(hoveringPos) && stack.equals(lastItem)
            && hoveringMaxed == maximiseTurn && lookAngle == hoveringAngle)
            return cached;

        PlacementInfo info = new PlacementInfo();
        hoveringMaxed = maximiseTurn;
        hoveringAngle = lookAngle;
        hoveringPos = pos2;
        lastItem = stack;
        cached = info;

        HypertubeBaseBlock track = (HypertubeBaseBlock) state2.getBlock();
        Pair<Vec3, Direction.AxisDirection> nearestTrackAxis = track.getNearestTrackAxis(level, pos2, state2, lookVec);
        Vec3 axis2 = nearestTrackAxis.getFirst()
                .scale(nearestTrackAxis.getSecond() == Direction.AxisDirection.POSITIVE ? -1 : 1);
        Vec3 normal2 = track.getUpNormal(level, pos2, state2)
                .normalize();
        Vec3 normedAxis2 = axis2.normalize();
        Vec3 end2 = track.getCurveStart(level, pos2, state2, axis2);


        ConnectingFrom connectingFrom = stack.get(ModDataComponent.TUBE_CONNECTING_FROM);

        BlockPos pos1 = connectingFrom.pos();
        Vec3 axis1 = connectingFrom.axis();
        Vec3 normedAxis1 = axis1.normalize();
        Vec3 end1 = connectingFrom.end();
        Vec3 normal1 = connectingFrom.normal();
        BlockState state1 = level.getBlockState(pos1);

        if (level.isClientSide) {
            info.end1 = end1;
            info.end2 = end2;
            info.normal1 = normal1;
            info.normal2 = normal2;
            info.axis1 = axis1;
            info.axis2 = axis2;
        }

        if (pos1.equals(pos2))
            return info.withMessage("second_point");
        if (pos1.distSqr(pos2) > maxLength * maxLength)
            return info.withMessage("too_far")
                    .tooJumbly();
        //if (!state1.hasProperty(TrackBlock.HAS_BE))
        //    return info.withMessage("original_missing");
        if (level.getBlockEntity(pos2) instanceof TrackBlockEntity tbe && tbe.isTilted())
            return info.withMessage("turn_start");

        if (axis1.dot(end2.subtract(end1)) < 0) {
            axis1 = axis1.scale(-1);
            normedAxis1 = normedAxis1.scale(-1);
            end1 = track.getCurveStart(level, pos1, state1, axis1);
            if (level.isClientSide) {
                info.end1 = end1;
                info.axis1 = axis1;
            }
        }

        double[] intersect = VecHelper.intersect(end1, end2, normedAxis1, normedAxis2, Direction.Axis.Y);
        boolean parallel = intersect == null;
        boolean skipCurve = false;

        if ((parallel && normedAxis1.dot(normedAxis2) > 0) || (!parallel && (intersect[0] < 0 || intersect[1] < 0))) {
            axis2 = axis2.scale(-1);
            normedAxis2 = normedAxis2.scale(-1);
            end2 = track.getCurveStart(level, pos2, state2, axis2);
            if (level.isClientSide) {
                info.end2 = end2;
                info.axis2 = axis2;
            }
        }

        Vec3 cross2 = normedAxis2.cross(new Vec3(0, 1, 0));

        double a1 = Mth.atan2(normedAxis2.z, normedAxis2.x);
        double a2 = Mth.atan2(normedAxis1.z, normedAxis1.x);
        double angle = a1 - a2;
        double ascend = end2.subtract(end1).y;
        double absAscend = Math.abs(ascend);
        boolean slope = !normal1.equals(normal2);

        if (level.isClientSide) {
            Vec3 offset1 = axis1.scale(info.end1Extent);
            Vec3 offset2 = axis2.scale(info.end2Extent);
            BlockPos targetPos1 = pos1.offset(BlockPos.containing(offset1));
            BlockPos targetPos2 = pos2.offset(BlockPos.containing(offset2));
            info.curve = new BezierConnection(Couple.create(targetPos1, targetPos2),
                    Couple.create(end1.add(offset1), end2.add(offset2)), Couple.create(normedAxis1, normedAxis2),
                    Couple.create(normal1, normal2), true, girder, TrackMaterial.fromItem(stack.getItem()));
        }

        // S curve or Straight

        double dist = 0;

        if (parallel) {
            double[] sTest = VecHelper.intersect(end1, end2, normedAxis1, cross2, Direction.Axis.Y);
            if (sTest != null) {
                double t = Math.abs(sTest[0]);
                double u = Math.abs(sTest[1]);

                skipCurve = Mth.equal(u, 0);

                if (!skipCurve && sTest[0] < 0)
                    return info.withMessage("perpendicular")
                            .tooJumbly();

                if (skipCurve) {
                    dist = VecHelper.getCenterOf(pos1)
                            .distanceTo(VecHelper.getCenterOf(pos2));
                    info.end1Extent = (int) Math.round((dist + 1) / axis1.length());

                } else {
                    if (!Mth.equal(ascend, 0) || normedAxis1.y != 0)
                        return info.withMessage("ascending_s_curve");

                    double targetT = u <= 1 ? 3 : u * 2;

                    if (t < targetT)
                        return info.withMessage("too_sharp");

                    // This is for standardising s curve sizes
                    if (t > targetT) {
                        int correction = (int) ((t - targetT) / axis1.length());
                        info.end1Extent = maximiseTurn ? 0 : correction / 2 + (correction % 2);
                        info.end2Extent = maximiseTurn ? 0 : correction / 2;
                    }
                }
            }
        }

        // Slope

        if (slope) {
            if (!skipCurve)
                return info.withMessage("slope_turn");
            if (Mth.equal(normal1.dot(normal2), 0))
                return info.withMessage("opposing_slopes");
            if ((axis1.y < 0 || axis2.y > 0) && ascend > 0)
                return info.withMessage("leave_slope_ascending");
            if ((axis1.y > 0 || axis2.y < 0) && ascend < 0)
                return info.withMessage("leave_slope_descending");

            skipCurve = false;
            info.end1Extent = 0;
            info.end2Extent = 0;

            Direction.Axis plane = Mth.equal(axis1.x, 0) ? Direction.Axis.X : Direction.Axis.Z;
            intersect = VecHelper.intersect(end1, end2, normedAxis1, normedAxis2, plane);
            double dist1 = Math.abs(intersect[0] / axis1.length());
            double dist2 = Math.abs(intersect[1] / axis2.length());

            if (dist1 > dist2)
                info.end1Extent = (int) Math.round(dist1 - dist2);
            if (dist2 > dist1)
                info.end2Extent = (int) Math.round(dist2 - dist1);

            double turnSize = Math.min(dist1, dist2);
            if (intersect[0] < 0 || intersect[1] < 0)
                return info.withMessage("too_sharp")
                        .tooJumbly();
            if (turnSize < 2)
                return info.withMessage("too_sharp");

            // This is for standardising curve sizes
            if (turnSize > 2 && !maximiseTurn) {
                info.end1Extent += turnSize - 2;
                info.end2Extent += turnSize - 2;
                turnSize = 2;
            }
        }

        // Straight ascend

        if (skipCurve && !Mth.equal(ascend, 0)) {
            int hDistance = info.end1Extent;
            if (axis1.y == 0 || !Mth.equal(absAscend + 1, dist / axis1.length())) {

                if (axis1.y != 0 && axis1.y == -axis2.y)
                    return info.withMessage("ascending_s_curve");

                info.end1Extent = 0;
                double minHDistance = Math.max(absAscend < 4 ? absAscend * 4 : absAscend * 3, 6) / axis1.length();
                if (hDistance < minHDistance)
                    return info.withMessage("too_steep");
                if (hDistance > minHDistance) {
                    int correction = (int) (hDistance - minHDistance);
                    info.end1Extent = maximiseTurn ? 0 : correction / 2 + (correction % 2);
                    info.end2Extent = maximiseTurn ? 0 : correction / 2;
                }

                skipCurve = false;
            }
        }

        // Turn

        if (!parallel) {
            float absAngle = Math.abs(AngleHelper.deg(angle));
            if (absAngle < 60 || absAngle > 300)
                return info.withMessage("turn_90")
                        .tooJumbly();

            intersect = VecHelper.intersect(end1, end2, normedAxis1, normedAxis2, Direction.Axis.Y);
            double dist1 = Math.abs(intersect[0]);
            double dist2 = Math.abs(intersect[1]);
            float ex1 = 0;
            float ex2 = 0;

            if (dist1 > dist2)
                ex1 = (float) ((dist1 - dist2) / axis1.length());
            if (dist2 > dist1)
                ex2 = (float) ((dist2 - dist1) / axis2.length());

            double turnSize = Math.min(dist1, dist2) - .1d;
            boolean ninety = (absAngle + .25f) % 90 < 1;

            if (intersect[0] < 0 || intersect[1] < 0)
                return info.withMessage("too_sharp")
                        .tooJumbly();

            double minTurnSize = ninety ? 7 : 3.25;
            double turnSizeToFitAscend =
                    minTurnSize + (ninety ? Math.max(0, absAscend - 3) * 2f : Math.max(0, absAscend - 1.5f) * 1.5f);

            if (turnSize < minTurnSize)
                return info.withMessage("too_sharp");
            if (turnSize < turnSizeToFitAscend)
                return info.withMessage("too_steep");

            // This is for standardising curve sizes
            if (!maximiseTurn) {
                ex1 += (turnSize - turnSizeToFitAscend) / axis1.length();
                ex2 += (turnSize - turnSizeToFitAscend) / axis2.length();
            }
            info.end1Extent = Mth.floor(ex1);
            info.end2Extent = Mth.floor(ex2);
            turnSize = turnSizeToFitAscend;
        }

        Vec3 offset1 = axis1.scale(info.end1Extent);
        Vec3 offset2 = axis2.scale(info.end2Extent);
        BlockPos targetPos1 = pos1.offset(BlockPos.containing(offset1));
        BlockPos targetPos2 = pos2.offset(BlockPos.containing(offset2));

        info.curve = skipCurve ? null
                : new BezierConnection(Couple.create(targetPos1, targetPos2),
                Couple.create(end1.add(offset1), end2.add(offset2)), Couple.create(normedAxis1, normedAxis2),
                Couple.create(normal1, normal2), true, girder, TrackMaterial.fromItem(stack.getItem()));

        info.valid = true;

        info.pos1 = pos1;
        info.pos2 = pos2;
        info.axis1 = axis1;
        info.axis2 = axis2;

        placeTracks(level, info, state1, state2, targetPos1, targetPos2, true);

        ItemStack offhandItem = player.getOffhandItem()
                .copy();
        boolean shouldPave = offhandItem.getItem() instanceof BlockItem && !AllTags.AllItemTags.INVALID_FOR_TRACK_PAVING.matches(offhandItem);
        if (shouldPave) {
            BlockItem paveItem = (BlockItem) offhandItem.getItem();
            paveTracks(level, info, paveItem, true);
            info.hasRequiredPavement = true;
        }

        if (level.isClientSide())
            return info;
        if (shouldPave) {
            BlockItem paveItem = (BlockItem) offhandItem.getItem();
            paveTracks(level, info, paveItem, false);
        }
        return placeTracks(level, info, state1, state2, targetPos1, targetPos2, false);
    }


    private static void paveTracks(Level level, PlacementInfo info, BlockItem blockItem, boolean simulate) {
        Block block = blockItem.getBlock();
        info.requiredPavement = 0;
        if (block == null || block instanceof EntityBlock || block.defaultBlockState()
                .getCollisionShape(level, info.pos1)
                .isEmpty())
            return;

        Set<BlockPos> visited = new HashSet<>();

        for (boolean first : Iterate.trueAndFalse) {
            int extent = (first ? info.end1Extent : info.end2Extent) + (info.curve != null ? 1 : 0);
            Vec3 axis = first ? info.axis1 : info.axis2;
            BlockPos pavePos = first ? info.pos1 : info.pos2;
            info.requiredPavement +=
                    TrackPaver.paveStraight(level, pavePos.below(), axis, extent, block, simulate, visited);
        }

        if (info.curve != null)
            info.requiredPavement += TrackPaver.paveCurve(level, info.curve, block, simulate, visited);
    }

    private static PlacementInfo placeTracks(Level level, PlacementInfo info, BlockState state1, BlockState state2,
                                                            BlockPos targetPos1, BlockPos targetPos2, boolean simulate) {
        info.requiredTracks = 0;

        for (boolean first : Iterate.trueAndFalse) {
            int extent = first ? info.end1Extent : info.end2Extent;
            Vec3 axis = first ? info.axis1 : info.axis2;
            BlockPos pos = first ? info.pos1 : info.pos2;
            BlockState state = first ? state1 : state2;
            if (state.hasProperty(TrackBlock.HAS_BE) && !simulate)
                state = state.setValue(TrackBlock.HAS_BE, false);

            //switch (state.getValue(TrackBlock.SHAPE)) {
            //    case TE, TW:
            //        state = state.setValue(TrackBlock.SHAPE, TrackShape.XO);
            //        break;
            //    case TN, TS:
            //        state = state.setValue(TrackBlock.SHAPE, TrackShape.ZO);
            //        break;
            //    default:
            //        break;
            //}

            for (int i = 0; i < (info.curve != null ? extent + 1 : extent); i++) {
                Vec3 offset = axis.scale(i);
                BlockPos offsetPos = pos.offset(BlockPos.containing(offset));
                BlockState stateAtPos = level.getBlockState(offsetPos);
                // copy over all shared properties from the shaped state to the correct track material block
                BlockState toPlace = BlockHelper.copyProperties(state, ModBlocks.HYPERTUBE.get().defaultBlockState());

                boolean canPlace = stateAtPos.canBeReplaced() || stateAtPos.is(BlockTags.FLOWERS);
                if (canPlace)
                    info.requiredTracks++;
                if (simulate)
                    continue;

                if (stateAtPos.getBlock() instanceof ITrackBlock trackAtPos) {
                    toPlace = trackAtPos.overlay(level, offsetPos, stateAtPos, toPlace);
                    canPlace = true;
                }

                if (canPlace)
                    level.setBlock(offsetPos, ProperWaterloggedBlock.withWater(level, toPlace, offsetPos), 3);
            }
        }

        if (info.curve == null)
            return info;

        if (!simulate) {
            BlockState onto = ModBlocks.HYPERTUBE.get().defaultBlockState();
            BlockState stateAtPos = level.getBlockState(targetPos1);
            level.setBlock(targetPos1, ProperWaterloggedBlock.withWater(level,
                    (AllTags.AllBlockTags.TRACKS.matches(stateAtPos) ? stateAtPos : BlockHelper.copyProperties(state1, onto))
                            .setValue(TrackBlock.HAS_BE, true), targetPos1), 3);

            stateAtPos = level.getBlockState(targetPos2);
            level.setBlock(targetPos2, ProperWaterloggedBlock.withWater(level,
                    (AllTags.AllBlockTags.TRACKS.matches(stateAtPos) ? stateAtPos : BlockHelper.copyProperties(state2, onto))
                            .setValue(TrackBlock.HAS_BE, true), targetPos2), 3);
        }

        BlockEntity te1 = level.getBlockEntity(targetPos1);
        BlockEntity te2 = level.getBlockEntity(targetPos2);
        int requiredTracksForTurn = (info.curve.getSegmentCount() + 1) / 2;

        if (!(te1 instanceof TrackBlockEntity tte1) || !(te2 instanceof TrackBlockEntity tte2)) {
            info.requiredTracks += requiredTracksForTurn;
            return info;
        }

        if (!tte1.getConnections()
                .containsKey(tte2.getBlockPos()))
            info.requiredTracks += requiredTracksForTurn;

        if (simulate)
            return info;

        tte1.addConnection(info.curve);
        tte2.addConnection(info.curve.secondary());
        tte1.tilt.tryApplySmoothing();
        tte2.tilt.tryApplySmoothing();
        return info;
    }*/

    @OnlyIn(Dist.CLIENT)
    public static void clientTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack stack = player.getMainHandItem();
        HitResult hitResult = Minecraft.getInstance().hitResult;

        if (hitResult == null)
            return;
        if (hitResult.getType() != HitResult.Type.BLOCK)
            return;

        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!stack.getItem().equals(ModItems.HYPERTUBE_ITEM.get())) {
            stack = player.getOffhandItem();
            hand = InteractionHand.OFF_HAND;
            if (!stack.getItem().equals(ModItems.HYPERTUBE_ITEM.get()))
                return;
        }

        if (!stack.hasFoil())
            return;

        Level level = player.level();
        BlockHitResult bhr = (BlockHitResult) hitResult;
        BlockPos pos = bhr.getBlockPos();
        BlockState hitState = level.getBlockState(pos);
        boolean hypertubeHitResult = hitState.getBlock() instanceof HypertubeBaseBlock;
        if (hitState.isAir() || hypertubeHitResult) {
            hoveringPos = pos;
        } else {
            pos = pos.relative(bhr.getDirection());
        }

        ConnectingFrom connectingFrom = stack.get(ModDataComponent.TUBE_CONNECTING_FROM);
        animation.setValue(0.8);
        if (connectingFrom == null) {
            animation.setValue(0);
            return;
        }
        BezierConnection bezierConnection = BezierConnection.of(connectingFrom, pos);

        if (!bezierConnection.isValid()) return;
        boolean hasException = false;
        if (bezierConnection.distance() > BezierConnection.MAX_DISTANCE) hasException = true;

        Vec3 pos1 = connectingFrom.pos().getCenter();
        int id = 0;

        Direction finalDirection = player.getDirection().getOpposite();

        if (hypertubeHitResult) {
            finalDirection = ((BlockHitResult) hitResult).getDirection().getOpposite();
        }

        float maxAngleBezierAngle = bezierConnection.getMaxAngleBezierAngle(finalDirection);
        if (maxAngleBezierAngle > 0.6 || hasException) {
            hasException = true;
            animation.setValue(0.2);
        } else {
            animation.setValue(0.8);
        }


        for (Vec3 bezierPoint : bezierConnection.getBezierPoints(finalDirection)) {
            line(id, pos1, bezierPoint, hasException);
            pos1 = bezierPoint;
            id++;
        }
    }


    @OnlyIn(Dist.CLIENT)
    private static void line(int id, Vec3 start, Vec3 end, boolean hasException) {
        int color = Color.mixColors(0xEA5C2B, 0x95CD41, animation.getValue());
        if (hasException) {
            Vec3 diff = end.subtract(start);
            start = start.add(diff.scale(0.2));
            end = start.add(diff.scale(-0.2));
        }
        Outliner.getInstance().showLine(Pair.of("start", id), start, end)
                .lineWidth(1 / 8f)
                .disableLineNormals()
                .colored(color);
    }


    @OnlyIn(Dist.CLIENT)
    public static void drawCustomBlockSelection(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {
        ItemStack mainHandItem = Minecraft.getInstance().player.getMainHandItem();
        if (!mainHandItem.is(ModBlocks.HYPERTUBE.get().asItem())) return;
        if (!mainHandItem.hasFoil()) return;
        ConnectingFrom connectingFrom = mainHandItem.get(ModDataComponent.TUBE_CONNECTING_FROM);
        if (connectingFrom == null) return;

        VertexConsumer vb = buffer.getBuffer(RenderType.lines());
        ms.pushPose();
        ms.translate(connectingFrom.pos().getX() - camera.x, connectingFrom.pos().getY() - camera.y, connectingFrom.pos().getZ() - camera.z);
        TrackBlockOutline.renderShape(HypertubeBlock.SHAPE_CORE, ms, vb, false);
        ms.popPose();
    }
}
