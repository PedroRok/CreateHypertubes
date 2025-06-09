package com.pedrorok.hypertube.items;

import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.managers.placement.BezierConnection;
import com.pedrorok.hypertube.managers.placement.ResponseDTO;
import com.pedrorok.hypertube.managers.placement.SimpleConnection;
import com.pedrorok.hypertube.managers.placement.TubePlacement;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModDataComponent;
import com.pedrorok.hypertube.utils.MessageUtils;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * @author Rok, Pedro Lucas nmm. Created on 23/04/2025
 * @project Create Hypertube
 */
public class HypertubeItem extends BlockItem {
    public HypertubeItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        BlockState state = level.getBlockState(pos);
        Player player = pContext.getPlayer();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (player == null)
            return super.useOn(pContext);
        if (pContext.getHand() == InteractionHand.OFF_HAND)
            return super.useOn(pContext);
        if (player.isShiftKeyDown() && !isFoil(stack))
            return super.useOn(pContext);

        Direction direction = pContext.getClickedFace();

        MessageUtils.sendActionMessage(player, "");
        if (!isFoil(stack)) {
            ResponseDTO select = select(level, pos, direction, stack);
            if (select.valid()) {
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);
                return InteractionResult.SUCCESS;
            }
            if (!select.errorMessage().isEmpty()) {
                MessageUtils.sendActionMessage(player, select.errorMessage());
            }
            return super.useOn(pContext);
        }

        SimpleConnection simpleConnection = ModDataComponent.decodeSimpleConnection(stack);
        if (player.isShiftKeyDown() && simpleConnection.pos().equals(pos)) {
            MessageUtils.sendActionMessage(player, "§eConnection cleared");
            clearConnection(stack);
            return InteractionResult.SUCCESS;
        }

        if (simpleConnection.pos().equals(pos)) {
            player.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0f, 1.0f);
            return InteractionResult.FAIL;
        }

        boolean isHypertubeClicked = (state.getBlock() instanceof HypertubeBlock);
        boolean success = true;

        if (isHypertubeClicked) {
            Optional<HypertubeBlockEntity> blockEntity = level.getBlockEntity(pos, ModBlockEntities.HYPERTUBE.get());
            if (blockEntity.isPresent()) {
                success = handleHypertubeClicked(blockEntity.get(), player, simpleConnection, pos, direction, level, stack);
            }
        }


        SoundType soundtype = state.getSoundType();
        if (success)
            level.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
                    (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

        return isHypertubeClicked ? InteractionResult.FAIL : super.useOn(pContext);
    }


    private boolean handleHypertubeClicked(HypertubeBlockEntity tubeEntity, Player player, SimpleConnection simpleConnection, BlockPos pos, Direction direction, Level level, ItemStack stack) {

        boolean thisTubeCanConnTo = tubeEntity.getConnectionTo() == null;
        boolean thisTubeCanConnFrom = tubeEntity.getConnectionFrom() == null;
        HypertubeBlockEntity otherBlockEntity = (HypertubeBlockEntity) level.getBlockEntity(simpleConnection.pos());

        if (otherBlockEntity == null) {
            player.displayClientMessage(Component.literal("No other tube found"), true);
            return false;
        }

        boolean otherTubeCanConnTo = otherBlockEntity.getConnectionTo() == null;
        boolean otherTubeCanConnFrom = otherBlockEntity.getConnectionFrom() == null;

        boolean usingConnectingTo = thisTubeCanConnFrom && otherTubeCanConnTo;

        if (!usingConnectingTo) {
            if (!thisTubeCanConnTo || !otherTubeCanConnFrom) {
                player.displayClientMessage(Component.literal("Both tubes are already connected"), true);
                return false;
            }
        }

        BezierConnection connection = new BezierConnection(
                usingConnectingTo ? simpleConnection : new SimpleConnection(pos, direction),
                usingConnectingTo ? new SimpleConnection(pos, direction.getOpposite()) : new SimpleConnection(simpleConnection.pos(), simpleConnection.direction().getOpposite()));


        ResponseDTO validation = connection.getValidation();
        if (validation.valid()) {
            validation = TubePlacement.checkSurvivalItems(player, (int) connection.distance(), true);
        }


        if (!validation.valid()) {
            player.displayClientMessage(Component.literal(validation.errorMessage()), true);
            return false;
        }
        TubePlacement.checkSurvivalItems(player, (int) connection.distance(), false);

        if (level.isClientSide) {
            connection.drawPath(LerpedFloat.linear()
                    .startWithValue(0));
        }

        if (usingConnectingTo) {
            tubeEntity.setConnectionFrom(connection.getFromPos(), direction);
            otherBlockEntity.setConnectionTo(connection);
        } else {
            tubeEntity.setConnectionTo(connection);
            otherBlockEntity.setConnectionFrom(connection.getFromPos(), direction);
        }

        player.displayClientMessage(Component.literal("Connected"), true);
        player.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);


        clearConnection(player.getItemInHand(InteractionHand.MAIN_HAND));
        return true;
    }

    @SuppressWarnings("DataFlowIssue")
    public BlockState getPlacementState(UseOnContext pContext) {
        return getPlacementState(updatePlacementContext(new BlockPlaceContext(pContext)));
    }

    public static ResponseDTO select(LevelAccessor world, BlockPos pos, Direction direction, ItemStack heldItem) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!(block instanceof HypertubeBlock tube))
            return ResponseDTO.get(false);
        HypertubeBlockEntity blockEntity = (HypertubeBlockEntity) world.getBlockEntity(pos);
        if (blockEntity == null) {
            return ResponseDTO.get(false);
        }
        if (!blockEntity.getFacesConnectable().contains(direction)) {
            return ResponseDTO.get(false, "§cTube can't connect to this face");
        }

        ModDataComponent.encodeSimpleConnection(pos,direction, heldItem);
        heldItem.getTag().putBoolean("foil", true);
        return ResponseDTO.get(true);
    }

    public static void clearConnection(ItemStack stack) {
        ModDataComponent.removeSimpleConnection(stack);
        stack.getTag().remove("foil");
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getTag().contains("foil") || stack.isEnchanted();
    }
}
