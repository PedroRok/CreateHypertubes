
package com.pedrorok.hypertube.core.smarttube;

import com.pedrorok.hypertube.blocks.ActionTubeBlock;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import com.pedrorok.hypertube.registry.ModPartialModels;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/11/2025
 * @project Create Hypertube
 */
public class RedstoneDetectorAttachment implements ISmartTubeAttachment {

    @Override
    public String getId() {
        return "redstone_input";
    }

    @Override
    public PartialModel getPartialModel() {
        return ModPartialModels.REDSTONE_DETECTOR;
    }
}