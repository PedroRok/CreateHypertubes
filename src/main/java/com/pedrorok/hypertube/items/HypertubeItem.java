package com.pedrorok.hypertube.items;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.blocks.HypertubeBaseBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.managers.placement.BezierConnection;
import com.pedrorok.hypertube.managers.placement.SimpleConnection;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModDataComponent;
import com.simibubi.create.AllDataComponents;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
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

        if (!isFoil(stack)) {

            if (select(level, pos, direction, stack)) {
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);
                return InteractionResult.SUCCESS;
            }
            return super.useOn(pContext);
        }

        SimpleConnection simpleConnection = stack.get(ModDataComponent.TUBE_CONNECTING_FROM);
        if (player.isShiftKeyDown() && simpleConnection.pos().equals(pos)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("Connection cleared"), true);
            } else {
                level.playSound(player, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.75f, 1);
            }
            clearConnection(stack);
            return InteractionResult.SUCCESS;
        }

        if (simpleConnection.pos().equals(pos)) {
            player.displayClientMessage(Component.literal("Can't connect to itself"), true);
            player.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0f, 1.0f);
            return InteractionResult.FAIL;
        }

        boolean isHypertubeClicked = (state.getBlock() instanceof HypertubeBlock);
        boolean success = true;

        if (isHypertubeClicked) {
            Optional<HypertubeBlockEntity> blockEntity = level.getBlockEntity(pos, ModBlockEntities.HYPERTUBE_ENTITY.get());
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
        connection.drawPath(LerpedFloat.linear()
                .startWithValue(0));
        if (!connection.isValid()) {
            player.displayClientMessage(Component.literal("Invalid connection"), true);
            return false;
        }

        if (usingConnectingTo) {
            tubeEntity.setConnectionFrom(connection.getFromPos());
            otherBlockEntity.setConnectionTo(connection);

        } else {
            tubeEntity.setConnectionTo(connection);
            otherBlockEntity.setConnectionFrom(connection.getFromPos());
        }


        player.displayClientMessage(Component.literal("Connected"), true);
        player.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);


        clearConnection(stack);
        return true;
    }

    @SuppressWarnings("DataFlowIssue")
    public BlockState getPlacementState(UseOnContext pContext) {
        return getPlacementState(updatePlacementContext(new BlockPlaceContext(pContext)));
    }

    public static boolean select(LevelAccessor world, BlockPos pos, Direction direction, ItemStack heldItem) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!(block instanceof HypertubeBlock tube))
            return false;
        HypertubeBlockEntity blockEntity = (HypertubeBlockEntity) world.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        if (!blockEntity.getFacesConnectable().contains(direction)) {
            HypertubeMod.LOGGER.debug("Tube can't connect to any face");
            return false;
        }

        heldItem.set(ModDataComponent.TUBE_CONNECTING_FROM, new SimpleConnection(pos, direction));
        heldItem.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return true;
    }

    public static void clearConnection(ItemStack stack) {
        stack.remove(ModDataComponent.TUBE_CONNECTING_FROM);
        stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(AllDataComponents.TRACK_CONNECTING_FROM) || stack.has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }
}
