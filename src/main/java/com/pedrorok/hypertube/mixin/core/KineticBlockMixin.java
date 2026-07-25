package com.pedrorok.hypertube.mixin.core;

import com.pedrorok.hypertube.core.travel.TravelConstants;
import com.pedrorok.hypertube.utils.VoxelUtils;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Rok, Pedro Lucas nmm. 25/07/2026
 * @project Create Hypertube
 */
@Mixin(BlockBehaviour.class)
public abstract class KineticBlockMixin {

    @Inject(method = "getCollisionShape", at = @At("RETURN"), cancellable = true)
    protected void createHypertube$getCollisionShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (state.getBlock() instanceof KineticBlock
                && context instanceof EntityCollisionContext ecc
                && ecc.getEntity() != null
                && ecc.getEntity().getPersistentData().getBoolean(TravelConstants.TRAVEL_TAG)) {
            cir.setReturnValue(VoxelUtils.empty());
        }
    }
}
