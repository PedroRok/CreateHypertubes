package com.pedrorok.hypertube.items;

import com.pedrorok.hypertube.blocks.ActionTubeBlock;
import com.pedrorok.hypertube.blocks.blockentities.parent.ActionTubeBlockEntity;
import com.pedrorok.hypertube.core.smarttube.ITubeAttachment;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.NoSuchElementException;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/11/2025
 * @project Create Hypertube
 */
public class TubeAttachmentItem extends Item {

    private final String attachmentKey;

    public TubeAttachmentItem(String attachmentKey, Properties pProperties) {
        super(pProperties);
        this.attachmentKey = attachmentKey;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        BlockState state = level.getBlockState(pos);
        Player player = pContext.getPlayer();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (player == null)
            return super.useOn(pContext);
        if (pContext.getHand() == InteractionHand.OFF_HAND)
            return super.useOn(pContext);

        Direction direction = pContext.getClickedFace();

        if (!(state.getBlock() instanceof ActionTubeBlock block)) return InteractionResult.FAIL;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ActionTubeBlockEntity actionTubeBE)) return InteractionResult.FAIL;
        if (actionTubeBE.hasTubeAttachment(direction)) {
            return InteractionResult.FAIL;
        }
        if (!block.canPlaceAttachment(state, level, pos, direction)) {
            return InteractionResult.FAIL;
        }

        ITubeAttachment smartTube = getTubeAttachment();
        if (smartTube == null) {
            throw new NoSuchElementException("SmartTube attachment " + attachmentKey + " not found");
        }
        actionTubeBE.addTubeAttachment(direction, smartTube);
        pContext.getItemInHand().consume(1, player);
        IWrenchable.playRotateSound(level, pos);
        return super.useOn(pContext);
    }

    public ITubeAttachment getTubeAttachment() {
        return ITubeAttachment.get(attachmentKey);
    }
}
