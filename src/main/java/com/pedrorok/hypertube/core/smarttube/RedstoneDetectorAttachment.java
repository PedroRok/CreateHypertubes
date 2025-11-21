
package com.pedrorok.hypertube.core.smarttube;

import com.pedrorok.hypertube.blocks.ActionTubeBlock;
import com.pedrorok.hypertube.blocks.blockentities.ActionTubeBlockEntity;
import com.pedrorok.hypertube.registry.ModItems;
import com.pedrorok.hypertube.registry.ModPartialModels;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/11/2025
 * @project Create Hypertube
 */
public class RedstoneDetectorAttachment implements ITubeAttachment {

    @Override
    public String getId() {
        return "redstone_input";
    }

    @Override
    public PartialModel getPartialModel(BlockState blockState, ActionTubeBlockEntity blockEntity, Direction facing) {
        if (ActionTubeBlock.hasSignalOnSide(blockEntity.getLevel(), blockEntity.getBlockPos(), facing) ) {
            return ModPartialModels.REDSTONE_DETECTOR_ACTIVE;
        }
        return ModPartialModels.REDSTONE_DETECTOR;
    }

    @Override
    public ItemStack getItemStack() {
        return ModItems.REDSTONE_DETECTOR.asStack();
    }
}