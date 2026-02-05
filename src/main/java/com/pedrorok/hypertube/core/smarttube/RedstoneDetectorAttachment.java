
package com.pedrorok.hypertube.core.smarttube;

import com.jozufozu.flywheel.core.PartialModel;
import com.pedrorok.hypertube.blocks.ActionTubeBlock;
import com.pedrorok.hypertube.blocks.blockentities.ActionTubeBlockEntity;
import com.pedrorok.hypertube.ponder.HypertubesPonderScenes;
import com.pedrorok.hypertube.registry.ModItems;
import com.pedrorok.hypertube.registry.ModPartialModels;
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
        if (HypertubesPonderScenes.isAnyPonderScreenOpen()) {
            if (blockState.getValue(ActionTubeBlock.POWER) > 0) {
                return ModPartialModels.REDSTONE_DETECTOR_ACTIVE;
            }
            return ModPartialModels.REDSTONE_DETECTOR;
        }
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