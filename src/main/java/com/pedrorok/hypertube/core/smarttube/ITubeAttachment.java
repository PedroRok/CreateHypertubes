package com.pedrorok.hypertube.core.smarttube;

import com.pedrorok.hypertube.blocks.blockentities.parent.ActionTubeBlockEntity;
import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/11/2025
 * @project Create Hypertube
 */
public interface ITubeAttachment {
    Map<String, ITubeAttachment> REGISTRY = new HashMap<>();

    String getId();
    default ITubeActionPoint getActionPoint(Direction attachedDirection) {
        return null;
    };

    default boolean emitRedstoneSignal() {
        return false;
    }

    PartialModel getPartialModel(BlockState blockState, ActionTubeBlockEntity blockEntity, Direction facing);
    ItemStack getItemStack();

    static void register(@NotNull ITubeAttachment smartTube) {
        if (REGISTRY.containsKey(smartTube.getId())) {
            throw new IllegalArgumentException("Duplicate smart tube ID: " + smartTube.getId());
        }
        REGISTRY.put(smartTube.getId(), smartTube);
    }

    @Nullable
    static ITubeAttachment get(String id) {
        return REGISTRY.get(id);
    }

    static void init() {
        register(new RedstoneDetectorAttachment());
        register(new TubeScannerAttachment());
    }
}