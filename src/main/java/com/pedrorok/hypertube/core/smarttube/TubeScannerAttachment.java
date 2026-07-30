package com.pedrorok.hypertube.core.smarttube;

import com.pedrorok.hypertube.blocks.ActionTubeBlock;
import com.pedrorok.hypertube.blocks.blockentities.parent.ActionTubeBlockEntity;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
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
public class TubeScannerAttachment implements ITubeAttachment {

    @Override
    public String getId() {
        return "tube_scanner";
    }

    @Override
    public boolean emitRedstoneSignal() {
        return true;
    }

    @Override
    public PartialModel getPartialModel(BlockState blockState, ActionTubeBlockEntity blockEntity, Direction facing) {
        boolean hasCog = blockEntity instanceof ICogWheel;
        if (blockState.getValue(ActionTubeBlock.POWER) > 0) {
            return hasCog ? ModPartialModels.TUBE_SCANNER_ACTIVE : ModPartialModels.TUBE_SCANNER_NO_COG_ACTIVE;
        }
        return hasCog ? ModPartialModels.TUBE_SCANNER : ModPartialModels.TUBE_SCANNER_NO_COG;
    }

    @Override
    public ITubeActionPoint getActionPoint(Direction attachedDirection) {
        return ((entity, mover, pos) -> {
            var level = entity.level();

            if (level.isClientSide) return;

            BlockState currentState = level.getBlockState(pos);

            if (currentState.hasProperty(ActionTubeBlock.POWER)) {
                int currentPower = currentState.getValue(ActionTubeBlock.POWER);
                int newPower = entity != null && entity.isBaby() ? 8 : 15;

                if (newPower < currentPower) return;
                if (newPower != currentPower) {
                    level.setBlock(pos, currentState.setValue(ActionTubeBlock.POWER, newPower), 3);
                }
                level.scheduleTick(pos, currentState.getBlock(), 20);
                level.updateNeighborsAt(pos, currentState.getBlock());
            }
        });
    }

    @Override
    public ItemStack getItemStack() {
        return ModItems.TUBE_SCANNER.asStack();
    }
}