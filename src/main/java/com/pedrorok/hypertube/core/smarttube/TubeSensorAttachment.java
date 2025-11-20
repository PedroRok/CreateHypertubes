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
public class TubeSensorAttachment implements ISmartTubeAttachment {

    @Override
    public String getId() {
        return "tube_sensor";
    }

    @Override
    public boolean emitRedstoneSignal() {
        return true;
    }

    @Override
    public PartialModel getPartialModel() {
        return ModPartialModels.REDSTONE_DETECTOR;
    }

    @Override
    public ITubeActionPoint getActionPoint(Direction attachedDirection) {
        return ((entity, mover, pos) -> {
            var level = entity.level();

            if (level.isClientSide) return;

            BlockState currentState = level.getBlockState(pos);

            if (currentState.hasProperty(ActionTubeBlock.POWER)) {
                int currentPower = currentState.getValue(ActionTubeBlock.POWER);
                int newPower = entity.isBaby() ? 8 : 15;

                if (newPower >= currentPower) {
                    if (newPower != currentPower) {
                        level.setBlock(pos, currentState.setValue(ActionTubeBlock.POWER, newPower), 3);
                    }
                    level.scheduleTick(pos, currentState.getBlock(), 20);
                    level.updateNeighborsAt(pos, currentState.getBlock());
                }
            }
        });
    }
}