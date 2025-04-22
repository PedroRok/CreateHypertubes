package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.events.TravelManager;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class HyperEntranceBlockEntity extends BlockEntity {

    private static final float RADIUS = 1.0f;

    public HyperEntranceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYPERTUBE_ENTRANCE_ENTITY.get(), pos, state);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        if (level.isClientSide) return;

        Optional<ServerPlayer> nearbyPlayers = getNearbyPlayers((ServerLevel) level, pos.getCenter());
        if (nearbyPlayers.isEmpty()) return;

        ServerPlayer player = nearbyPlayers.get();
        TravelManager.tryStartTravel(player, pos, state);
    }

    private static Optional<ServerPlayer> getNearbyPlayers(ServerLevel level, Vec3 centerPos) {
        return level.players().stream()
                .filter(player -> player.distanceToSqr(centerPos.x, centerPos.y, centerPos.z) < RADIUS)
                .findFirst();
    }

}
