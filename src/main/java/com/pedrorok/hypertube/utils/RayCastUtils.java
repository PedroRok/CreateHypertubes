package com.pedrorok.hypertube.utils;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/**
 * @author Rok, Pedro Lucas nmm. Created on 25/04/2025
 * @project Create Hypertube
 */
public class RayCastUtils {

    public static <T extends Block> Direction getDirectionFromHitResult(Player player, @Nullable T filter) {
        return getDirectionFromHitResult(player, filter, false);
    }

    public static <T extends Block> Direction getDirectionFromHitResult(Player player, @Nullable T filter, boolean ignoreFilter) {
        HitResult hitResult = player.pick(5, 0, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return getFromPlayer(player);
        }
        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        Level level = player.level();
        if ((filter != null && !level.getBlockState(blockHitResult.getBlockPos()).is(filter)) || ignoreFilter) {
            return getFromPlayer(player);
        }
        return blockHitResult.getDirection().getOpposite();
    }

    private static Direction getFromPlayer(Player player) {
        Direction direction = player.getDirection().getOpposite();
        if (player.getXRot() < -45) {
            direction = Direction.DOWN;
        } else if (player.getXRot() > 45) {
            direction = Direction.UP;
        }
        return direction;
    }
}
