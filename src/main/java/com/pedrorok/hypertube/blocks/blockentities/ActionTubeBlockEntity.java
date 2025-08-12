package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.blocks.HyperEntranceBlock;
import com.pedrorok.hypertube.core.sound.TubeSoundManager;
import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.registry.ModParticles;
import com.pedrorok.hypertube.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 11/08/2025
 * @project Create Hypertube
 */
public abstract class ActionTubeBlockEntity extends TubeBlockEntity {

    private static final float RADIUS = 1.0f;
    protected final UUID tubeSoundId = UUID.randomUUID();


    public ActionTubeBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }


    protected void spawnSuctionParticle(Level level, BlockPos blockPos, Direction face) {
        face = face.getOpposite();
        Vec3 center = Vec3.atCenterOf(blockPos);
        RandomSource rand = level.getRandom();

        Vec3 faceNormal = Vec3.atLowerCornerOf(face.getNormal());

        double spread = 0.5;

        Vec3 tangentA = switch (face.getAxis()) {
            case Y, Z -> new Vec3(1, 0, 0);
            default -> new Vec3(0, 1, 0);
        };
        Vec3 tangentB = faceNormal.cross(tangentA).normalize();
        double offsetA = (rand.nextDouble() - 0.5) * 2 * spread;
        double offsetB = (rand.nextDouble() - 0.5) * 2 * spread;
        Vec3 randomOffset = tangentA.scale(offsetA).add(tangentB.scale(offsetB));

        Vec3 start = center.add(faceNormal.scale(1 + level.random.nextFloat())).add(randomOffset);
        Vec3 motion = center.subtract(start).normalize().scale(0.05);


        level.addParticle(
                ModParticles.SUCTION_PARTICLE.get(),
                start.x, start.y, start.z,
                motion.x, motion.y, motion.z
        );
    }


    protected void playOpenCloseSound(boolean open) {
        RandomSource random = level.random;
        float pitch = 0.4F + random.nextFloat() * 0.4F;
        level.playSound(null, this.getBlockPos(), open ? ModSounds.HYPERTUBE_ENTRANCE_OPEN.get() : ModSounds.HYPERTUBE_ENTRANCE_CLOSE.get(), SoundSource.BLOCKS, 0.2f, pitch);
    }


    @OnlyIn(Dist.CLIENT)
    protected void playClientEffects(TubeSoundManager.TubeAmbientSound sound) {
        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();

        boolean isOpen = state.getValue(HyperEntranceBlock.OPEN);

        LocalPlayer player = Minecraft.getInstance().player;
        Vec3 source = pos.getCenter();
        Vec3 listener = player.position();

        Vec3 worldDirection = source.subtract(listener).normalize();

        Vec3 forward = player.getLookAngle().normalize();
        Vec3 up = player.getUpVector(1.0F).normalize();
        Vec3 right = forward.cross(up).normalize();

        double x = worldDirection.dot(right);
        double y = worldDirection.dot(up);
        double z = worldDirection.dot(forward);

        Vec3 rotatedDirection = new Vec3(x, y, z).normalize();

        double distance = player.distanceToSqr(source);

        if (isOpen) {
            spawnSuctionParticle(level, pos, state.getValue(HyperEntranceBlock.FACING));
        }

        sound.enableClientPlayerSound(
                player,
                rotatedDirection,
                distance,
                isOpen
        );
    }

    @Nullable
    protected LivingEntity getInRangeLivingEntities(ServerLevel level, Vec3 centerPos, Direction facing) {
        Vec3 checkPos = centerPos.add(Vec3.atLowerCornerOf(facing.getOpposite().getNormal()));

        return level.getNearestEntity(
                level.getEntitiesOfClass(LivingEntity.class,
                        AABB.ofSize(checkPos, (RADIUS - 0.25) * 2, (RADIUS - 0.25) * 2, (RADIUS - 0.25) * 2),
                        (entity) -> TravelConstants.TRAVELLER_ENTITIES.contains(entity.getType())),
                TargetingConditions.forNonCombat().ignoreLineOfSight(),
                null,
                centerPos.x, centerPos.y, centerPos.z);
    }

    @Nullable
    protected LivingEntity getNearbyLivingEntities(ServerLevel level, Vec3 centerPos) {
        return level.getNearestEntity(
                level.getEntitiesOfClass(LivingEntity.class,
                        AABB.ofSize(centerPos, RADIUS * 6, RADIUS * 6, RADIUS * 6),
                        (entity) -> TravelConstants.TRAVELLER_ENTITIES.contains(entity.getType())),
                TargetingConditions.forNonCombat().ignoreLineOfSight(),
                null,
                centerPos.x, centerPos.y, centerPos.z);
    }


    /**
     * @return true if the tube is closed, false if it was opened
     */
    protected boolean isTubeClosed(boolean canOpen, boolean isOpen) {
        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();
        if (!canOpen) {
            if (isOpen) {
                level.setBlock(pos, state.setValue(HyperEntranceBlock.OPEN, false), 3);
                playOpenCloseSound(false);
            }
            return true;
        }

        if (!isOpen) {
            level.setBlock(pos, state.setValue(HyperEntranceBlock.OPEN, true), 3);
            playOpenCloseSound(true);
        }
        return false;
    }


    @Override
    public void remove() {
        if (level.isClientSide) {
            removeClient();
        }
        super.remove();
    }

    @OnlyIn(Dist.CLIENT)
    private void removeClient() {
        TubeSoundManager.getAmbientSound(tubeSoundId).stopSound();
        TubeSoundManager.removeAmbientSound(tubeSoundId);
    }
}
