
package com.pedrorok.hypertube.core.smarttube;

import com.pedrorok.hypertube.blocks.ActionTubeBlock;
import com.pedrorok.hypertube.blocks.blockentities.parent.ActionTubeBlockEntity;
import com.pedrorok.hypertube.ponder.HypertubesPonderPlugin;
import com.pedrorok.hypertube.registry.ModItems;
import com.pedrorok.hypertube.registry.ModPartialModels;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
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
        boolean hasCog = blockEntity instanceof ICogWheel;
        if (HypertubesPonderPlugin.isAnyPonderScreenOpen()) {
            if (blockState.getValue(ActionTubeBlock.POWER) > 0) {
                return hasCog ? ModPartialModels.REDSTONE_DETECTOR_ACTIVE : ModPartialModels.REDSTONE_DETECTOR_NO_COG_ACTIVE;
            }
            return hasCog ? ModPartialModels.REDSTONE_DETECTOR : ModPartialModels.REDSTONE_DETECTOR_NO_COG;
        }
        if (ActionTubeBlock.hasSignalOnSide(blockEntity.getLevel(), blockEntity.getBlockPos(), facing)) {
            return  hasCog ? ModPartialModels.REDSTONE_DETECTOR_ACTIVE : ModPartialModels.REDSTONE_DETECTOR_NO_COG_ACTIVE;
        }
        return  hasCog ? ModPartialModels.REDSTONE_DETECTOR : ModPartialModels.REDSTONE_DETECTOR_NO_COG;
    }

    @Override
    public ItemStack getItemStack() {
        return ModItems.REDSTONE_DETECTOR.asStack();
    }
}