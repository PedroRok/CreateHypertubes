package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.managers.TravelManager;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * @author Rok, Pedro Lucas nmm. Created on 21/04/2025
 * @project Create Hypertube
 */
public class HyperEntranceBlockEntity extends BlockEntity {

    private static final float RADIUS = 1.0f;

    public HyperEntranceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        if (level.isClientSide) {
            return;
        }


        if (level.getServer().getTickCount() % (20 * 7) == 0) { // Play sound every 2 seconds
            for (Player oPlayer : level.players()) {
                ((ServerPlayer) oPlayer).connection.send(new ClientboundSoundPacket(ModSounds.WIND_TUNNEL,
                        SoundSource.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 0.2f, 1.3f, 5));
            }
        }

        Optional<ServerPlayer> nearbyPlayers = getNearbyPlayers((ServerLevel) level, pos.getCenter());
        if (nearbyPlayers.isEmpty()) return;

        ServerPlayer player = nearbyPlayers.get();
        if (player.isShiftKeyDown()) return;
        TravelManager.tryStartTravel(player, pos, state);
    }

    private static Optional<ServerPlayer> getNearbyPlayers(ServerLevel level, Vec3 centerPos) {
        return level.players().stream()
                .filter(player -> player.distanceToSqr(centerPos.x, centerPos.y, centerPos.z) < RADIUS)
                .findFirst();
    }

}
