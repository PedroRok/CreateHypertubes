package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.core.connection.SimpleConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * @author Rok, Pedro Lucas nmm. Created on 23/04/2025
 * @project Create Hypertube
 */
public class ModDataComponent {

    public static final String TUBE_SIMPLE_POS = "tube_simple_pos";
    public static final String TUBE_SIMPLE_DIR = "tube_simple_dir";
    public static final String TUBE_SIMPLE_OFFSET = "tube_simple_offset";

    public static void encodeSimpleConnection(SimpleConnection connection, ItemStack stack) {
        encodeSimpleConnection(connection.pos(), connection.direction(), connection.offset(), stack);
    }

    public static void encodeSimpleConnection(BlockPos pos, Direction direction, ItemStack stack) {
        encodeSimpleConnection(pos, direction, 0f, stack);
    }

    public static void encodeSimpleConnection(BlockPos pos, Direction direction, float offset, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(TUBE_SIMPLE_POS, pos.asLong());
        tag.putInt(TUBE_SIMPLE_DIR, direction.ordinal());
        tag.putFloat(TUBE_SIMPLE_OFFSET, offset);
    }

    public static SimpleConnection decodeSimpleConnection(ItemStack stack) {
        if (!stack.hasTag()) return null;
        CompoundTag tag = stack.getTag();
        if (!tag.contains(TUBE_SIMPLE_POS)) return null;
        long pos = tag.getLong(TUBE_SIMPLE_POS);
        int dir = tag.getInt(TUBE_SIMPLE_DIR);
        float offset = tag.getFloat(TUBE_SIMPLE_OFFSET);
        return new SimpleConnection(BlockPos.of(pos), Direction.values()[dir], offset);
    }

    public static void removeSimpleConnection(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove(TUBE_SIMPLE_POS);
            stack.getTag().remove(TUBE_SIMPLE_DIR);
            stack.getTag().remove(TUBE_SIMPLE_OFFSET);
        }
    }
}
