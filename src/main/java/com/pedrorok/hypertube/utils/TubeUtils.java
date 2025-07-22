package com.pedrorok.hypertube.utils;

import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.ConnDTO;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import com.pedrorok.hypertube.core.placement.ResponseDTO;
import com.pedrorok.hypertube.items.HypertubeItem;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModDataComponent;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 11/06/2025
 * @project Create Hypertube
 */
public class TubeUtils {


    public static ResponseDTO checkClickedHypertube(Level level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof HypertubeBlockEntity tubeEntity
            && !tubeEntity.getFacesConnectable().contains(direction)) {
            return ResponseDTO.invalid("placement.create_hypertube.cant_conn_to_face");
        }
        return ResponseDTO.get(true);
    }

    public static boolean checkPlayerPlacingBlock(@NotNull Player player, Level level, BlockPos pos) {

        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.getItem() != ModBlocks.HYPERTUBE.asItem()) {
            return true;
        }
        if (!itemInHand.hasFoil()) {
            return true;
        }

        ConnDTO connectionFromDTO = itemInHand.get(ModDataComponent.TUBE_CONNECTING_FROM);

        Direction finalDirection = RayCastUtils.getDirectionFromHitResult(player, null, true);
        SimpleConnection connectionTo = new SimpleConnection(pos, finalDirection);
        BezierConnection bezierConnection = BezierConnection.of(connectionFromDTO.toSimpleConnection(pos), connectionTo);

        return checkPlayerPlacingBlockValidation(player, bezierConnection, level);
    }

    public static boolean checkPlayerPlacingBlockValidation(Player player, @NotNull BezierConnection bezierConnection, Level level) {
        ResponseDTO validation = bezierConnection.getValidation();
        if (validation.valid()) {
            validation = checkSurvivalItems(player, (int) bezierConnection.distance(), true);
        }

        if (validation.valid()) {
            validation = checkBlockCollision(level, bezierConnection);
        }

        if (!validation.valid()) {
            MessageUtils.sendActionMessage(player, validation.getMessageComponent());
            return false;
        }
        HypertubeItem.clearConnection(player.getItemInHand(InteractionHand.MAIN_HAND));

        checkSurvivalItems(player, (int) bezierConnection.distance() + 1, false);
        return true;
    }


    private static final float CHECK_DISTANCE_THRESHOLD = 0.4f;

    public static ResponseDTO checkBlockCollision(@NotNull Level level, @NotNull BezierConnection bezierConnection) {
        List<Vec3> positions = new ArrayList<>(bezierConnection.getBezierPoints(false).reversed());
        positions.removeLast();
        positions.removeFirst();

        for (int i = 1; i < positions.size() - 1; i++) {
            Vec3 pos = positions.get(i);
            if (hasCollision(level, pos) ||
                hasCollision(level, pos.add(CHECK_DISTANCE_THRESHOLD, 0, 0)) ||
                hasCollision(level, pos.add(0, 0, CHECK_DISTANCE_THRESHOLD)) ||
                hasCollision(level, pos.add(CHECK_DISTANCE_THRESHOLD, 0, CHECK_DISTANCE_THRESHOLD)) ||
                hasCollision(level, pos.add(-CHECK_DISTANCE_THRESHOLD, 0, 0)) ||
                hasCollision(level, pos.add(0, 0, -CHECK_DISTANCE_THRESHOLD)) ||
                hasCollision(level, pos.add(-CHECK_DISTANCE_THRESHOLD, 0, -CHECK_DISTANCE_THRESHOLD))) {
                return ResponseDTO.invalid("placement.create_hypertube.block_collision");
            }
        }
        return ResponseDTO.get(true);
    }

    private static boolean hasCollision(Level level, Vec3 pos) {
        BlockPos blockPos = BlockPos.containing(pos);
        boolean hasCollision = !level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty();
        if (hasCollision && level.isClientSide) {
            outlineBlocks(blockPos);
        }
        return hasCollision;
    }


    public static ResponseDTO checkSurvivalItems(@NotNull Player player, int neededTubes, boolean simulate) {
        if (!player.isCreative()
            && !checkPlayerInventory(player, neededTubes, simulate)) {
            return ResponseDTO.invalid("placement.create_hypertube.no_enough_tubes");
        }
        return ResponseDTO.get(true);
    }

    private static boolean checkPlayerInventory(@NotNull Player player, int neededTubes, boolean simulate) {
        int foundTubes = 0;

        Inventory inv = player.getInventory();
        int size = inv.items.size();
        for (int j = 0; j <= size + 1; j++) {
            int i = j;
            boolean offhand = j == size + 1;
            if (j == size)
                i = inv.selected;
            else if (offhand)
                i = 0;
            else if (j == inv.selected)
                continue;

            ItemStack stackInSlot = (offhand ? inv.offhand : inv.items).get(i);
            boolean isTube = ModBlocks.HYPERTUBE.asStack().is(stackInSlot.getItem());
            if (!isTube)
                continue;
            if (foundTubes >= neededTubes)
                continue;

            int count = stackInSlot.getCount();

            if (!simulate) {
                int remainingItems =
                        count - Math.min(neededTubes - foundTubes, count);
                ItemStack newItem = stackInSlot.copyWithCount(remainingItems);
                if (offhand)
                    player.setItemInHand(InteractionHand.OFF_HAND, newItem);
                else
                    inv.setItem(i, newItem);
            }

            foundTubes += count;
        }
        return foundTubes >= neededTubes;
    }


    @OnlyIn(Dist.CLIENT)
    public static void outlineBlocks(BlockPos pos) {
        Outliner.getInstance().showAABB(pos.asLong(), new AABB(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                        pos.getX(), pos.getY(), pos.getZ()))
                .colored(0xEA5C2B)
                .lineWidth(1 / 8f)
                .disableLineNormals();
    }

    @OnlyIn(Dist.CLIENT)
    public static void line(UUID uuid, int id, Vec3 start, Vec3 end, LerpedFloat animation, boolean hasException) {
        int color = Color.mixColors(0xEA5C2B, 0x95CD41, animation.getValue());
        if (hasException) {
            Vec3 diff = end.subtract(start);
            start = start.add(diff.scale(0.2));
            end = start.add(diff.scale(-0.2));
        }
        Outliner.getInstance().showLine(Pair.of(uuid, id), start, end)
                .lineWidth(1 / 8f)
                .disableLineNormals()
                .colored(color);
    }
}
