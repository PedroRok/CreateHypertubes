package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.managers.TravelManager;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModSounds;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
public class HyperEntranceBlockEntity extends KineticBlockEntity {

    private static final float RADIUS = 1.0f;

    public HyperEntranceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
    }


    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        if (level.isClientSide) {
            return;
        }

        if (level.getServer().getTickCount() % (20 * 7) == 0) {
            for (Player oPlayer : level.players()) {
                ((ServerPlayer) oPlayer).connection.send(new ClientboundSoundPacket(ModSounds.WIND_TUNNEL,
                        SoundSource.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 0.2f, 1.3f, 5));
            }
        }

        Optional<ServerPlayer> nearbyPlayer = getNearbyPlayers((ServerLevel) level, pos.getCenter());
        if (nearbyPlayer.isEmpty()) {
            if (state.getValue(HyperEntranceBlock.OPEN)) {
                level.setBlock(pos, state.setValue(HyperEntranceBlock.OPEN, false), 3);
            }
            return;
        }
        if (!state.getValue(HyperEntranceBlock.OPEN)) {
            level.setBlock(pos, state.setValue(HyperEntranceBlock.OPEN, true), 3);
        }

        Optional<ServerPlayer> inRangePlayer = getInRangePlayers((ServerLevel) level, pos.getCenter());
        if (inRangePlayer.isEmpty()) return;

        ServerPlayer player = inRangePlayer.get();
        if (player.isShiftKeyDown()) return;
        TravelManager.tryStartTravel(player, pos, state);
    }

    private static Optional<ServerPlayer> getInRangePlayers(ServerLevel level, Vec3 centerPos) {
        return level.players().stream()
                .filter(player -> player.getBoundingBox()
                        .inflate(RADIUS)
                        .contains(centerPos))
                .findFirst();
    }

    private static Optional<ServerPlayer> getNearbyPlayers(ServerLevel level, Vec3 centerPos) {
        return level.players().stream()
                .filter(player -> player.getBoundingBox()
                        .inflate(RADIUS * 3)
                        .contains(centerPos))
                .findFirst();
    }

}
