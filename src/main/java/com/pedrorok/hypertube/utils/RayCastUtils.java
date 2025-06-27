package com.pedrorok.hypertube.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 25/04/2025
 * @project Create Hypertube
 */
public class RayCastUtils {

    public static Direction getDirectionFromHitResult(Player player, @Nullable Supplier<Boolean> filterSupplier) {
        return getDirectionFromHitResult(player, filterSupplier, false);
    }

    public static Direction getDirectionFromHitResult(Player player, @Nullable Supplier<Boolean> filter, boolean ignoreFilter) {
        HitResult hitResult = player.pick(5, 0, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return getFromPlayer(player, null);
        }
        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        if (filter != null && (!filter.get() || ignoreFilter)) {
            return getFromPlayer(player, blockHitResult.getBlockPos().relative(blockHitResult.getDirection()));
        }
        return blockHitResult.getDirection().getOpposite();
    }

    private static Direction getFromPlayer(Player player, BlockPos pos) {
        Direction direction = player.getDirection().getOpposite();
        boolean usingUpOrDown = false;
        if (player.getXRot() < -45) {
            direction = Direction.DOWN;
            usingUpOrDown = true;
        } else if (player.getXRot() > 45) {
            direction = Direction.UP;
            usingUpOrDown = true;
        }
        if (pos != null && usingUpOrDown) {
            BlockPos relative = pos.relative(direction.getOpposite());
            if (player.level().getBlockState(relative).isSolid()) {
                direction = player.getDirection().getOpposite();
            }
        }
        return direction;
    }
}
