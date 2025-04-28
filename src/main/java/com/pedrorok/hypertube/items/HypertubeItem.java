package com.pedrorok.hypertube.items;

import com.pedrorok.hypertube.blocks.HypertubeBaseBlock;
import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.managers.placement.BezierConnection;
import com.pedrorok.hypertube.managers.placement.SimpleConnection;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModDataComponent;
import com.simibubi.create.AllDataComponents;
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
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("Can't connect to itself"), true);
            } else {
                level.playSound(player, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.75f, 1);
            }
            return InteractionResult.FAIL;
        }

        boolean isHypertubeClicked = (state.getBlock() instanceof HypertubeBlock);

        if (isHypertubeClicked) {
            level.getBlockEntity(pos, ModBlockEntities.HYPERTUBE_ENTITY.get())
                    .ifPresent(hyperTubeEntity -> {
                        if (!level.isClientSide) {
                            BezierConnection connection = new BezierConnection(new SimpleConnection(pos, direction.getOpposite()), simpleConnection);
                            if (!connection.isValid()) {
                                player.displayClientMessage(Component.literal("Invalid connection"), true);
                                return;
                            }
                            hyperTubeEntity.setConnectionTo(connection);
                            HypertubeBlockEntity otherBlockEntity = (HypertubeBlockEntity) level.getBlockEntity(simpleConnection.pos());
                            if (otherBlockEntity != null) {
                                otherBlockEntity.setConnectionFrom(connection.getFromPos());
                            }
                            player.displayClientMessage(Component.literal("Connected"), true);
                        } else {
                            level.playSound(player, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);
                        }
                        clearConnection(stack);
                    });
        } else {
            // TODO: place block and define bezier connection
        }

        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        SoundType soundtype = state.getSoundType();
        if (soundtype != null)
            level.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
                    (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

        return isHypertubeClicked ? InteractionResult.FAIL : super.useOn(pContext);
    }

    public BlockState getPlacementState(UseOnContext pContext) {
        return getPlacementState(updatePlacementContext(new BlockPlaceContext(pContext)));
    }

    public static boolean select(LevelAccessor world, BlockPos pos, Direction direction, ItemStack heldItem) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!(block instanceof HypertubeBaseBlock track))
            return false;

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
