package com.pedrorok.hypertube.items;

import com.pedrorok.hypertube.blocks.HypertubeBlock;
import com.pedrorok.hypertube.blocks.blockentities.HypertubeBlockEntity;
import com.pedrorok.hypertube.managers.placement.ResponseDTO;
import com.pedrorok.hypertube.managers.connection.SimpleConnection;
import com.pedrorok.hypertube.managers.placement.TubePlacement;
import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.registry.ModDataComponent;
import com.pedrorok.hypertube.utils.MessageUtils;
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
                MessageUtils.sendActionMessage(player, select.getMessageComponent());
            }
            return super.useOn(pContext);
        }

        SimpleConnection simpleConnection = stack.get(ModDataComponent.TUBE_CONNECTING_FROM);
        if (player.isShiftKeyDown()) {
            MessageUtils.sendActionMessage(player, Component.translatable("placement.create_hypertube.conn_cleared").withColor(0xFFFF00));
            clearConnection(stack);
            return InteractionResult.SUCCESS;
        }

        if (simpleConnection.pos().equals(pos)) {
            player.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0f, 1.0f);
            return InteractionResult.FAIL;
        }

        boolean isHypertubeClicked = (state.getBlock() instanceof HypertubeBlock);
        boolean success = false;

        if (isHypertubeClicked) {
            Optional<HypertubeBlockEntity> blockEntity = level.getBlockEntity(pos, ModBlockEntities.HYPERTUBE.get());
            if (blockEntity.isPresent()) {
                success = TubePlacement.handleHypertubeClicked(blockEntity.get(), player, simpleConnection, pos, direction, level, stack);
            }
            SoundType soundtype = state.getSoundType();
            if (success) {
                level.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
                        (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            } else {
                level.playSound(player, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS,
                        1, 0.5f);
            }
        }

        return isHypertubeClicked ? InteractionResult.FAIL : super.useOn(pContext);
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
            return ResponseDTO.get(false, "placement.create_hypertube.cant_conn_to_face");
        }

        heldItem.set(ModDataComponent.TUBE_CONNECTING_FROM, new SimpleConnection(pos, direction));
        heldItem.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return ResponseDTO.get(true);
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
