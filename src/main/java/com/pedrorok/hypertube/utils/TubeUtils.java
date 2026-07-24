package com.pedrorok.hypertube.utils;

import com.pedrorok.hypertube.core.collision.TubeCollision;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.core.connection.SimpleConnection;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeConnectionEntity;
import com.pedrorok.hypertube.core.placement.ResponseDTO;
import com.pedrorok.hypertube.items.HypertubeItem;
import com.pedrorok.hypertube.registry.ModBlocks;
import com.pedrorok.hypertube.registry.ModDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rok, Pedro Lucas nmm. Created on 11/06/2025
 * @project Create Hypertube
 */
public class TubeUtils {

    public static float SPEED_MULTIPLIER = 1;


    public static ResponseDTO checkClickedHypertube(Level level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof ITubeConnectionEntity tubeEntity
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

        SimpleConnection connectionFrom = itemInHand.get(ModDataComponent.TUBE_CONNECTING_FROM);

        Direction finalDirection = RayCastUtils.getDirectionFromHitResult(player, null, true);
        SimpleConnection connectionTo = new SimpleConnection(pos, finalDirection, 0);
        BezierConnection bezierConnection = BezierConnection.of(connectionFrom, connectionTo);
        if (bezierConnection.isAngleTooHigh()) {
            BezierConnection newBezierConnection = BezierConnection.of(connectionFrom, new SimpleConnection(pos, finalDirection.getOpposite(), 0));
            if (!newBezierConnection.isAngleTooHigh()) {
                bezierConnection = newBezierConnection;
            }
        }
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


    public static ResponseDTO checkBlockCollision(@NotNull Level level, @NotNull BezierConnection bezierConnection) {
        boolean collision = false;
        for (BlockPos blockPos : TubeCollision.occupied(bezierConnection.getBezierPoints(level, bezierConnection.getFromPos().pos()))) {
            if (hasCollision(level, blockPos)) {
                if (!level.isClientSide) {
                    return ResponseDTO.invalid("placement.create_hypertube.block_collision");
                }
                collision = true;
            }
        }
        return collision
                ? ResponseDTO.invalid("placement.create_hypertube.block_collision")
                : ResponseDTO.get(true);
    }

    private static boolean hasCollision(Level level, BlockPos blockPos) {
        boolean hasCollision = !level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty();
        if (hasCollision && level.isClientSide) {
            BezierConnection.outlineBlocks(blockPos);
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


    public static float calculateTravelSpeed(float tubeSpeed) {
        tubeSpeed = tubeSpeed * SPEED_MULTIPLIER;
        return (0.4333f * Math.min(1, tubeSpeed / 16)) + (tubeSpeed / 240f);
    }
}
