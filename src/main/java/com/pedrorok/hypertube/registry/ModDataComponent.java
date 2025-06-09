package com.pedrorok.hypertube.registry;

import com.pedrorok.hypertube.managers.placement.SimpleConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * @author Rok, Pedro Lucas nmm. Created on 23/04/2025
 * @project Create Hypertube
 */
public class ModDataComponent {

    public static final String TUBE_SIMPLE_POS = "tube_simple_pos";
    public static final String TUBE_SIMPLE_DIR = "tube_simple_dir";

    public static void encodeSimpleConnection(SimpleConnection connection, ItemStack stack) {
        encodeSimpleConnection(connection.pos(), connection.direction(), stack);
    }

    public static void encodeSimpleConnection(BlockPos pos, Direction direction, ItemStack stack) {
        stack.getOrCreateTag().putLong(TUBE_SIMPLE_POS, pos.asLong());
        stack.getOrCreateTag().putInt(TUBE_SIMPLE_DIR, direction.ordinal());
    }

    public static SimpleConnection decodeSimpleConnection(ItemStack stack) {
        if (!stack.hasTag()) return null;
        long pos = stack.getOrCreateTag().getLong(TUBE_SIMPLE_POS);
        int dir = stack.getOrCreateTag().getInt(TUBE_SIMPLE_DIR);
        return new SimpleConnection(BlockPos.of(pos), Direction.values()[dir]);
    }

    public static void removeSimpleConnection(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getOrCreateTag().remove(TUBE_SIMPLE_POS);
            stack.getOrCreateTag().remove(TUBE_SIMPLE_DIR);
        }
    }
}
