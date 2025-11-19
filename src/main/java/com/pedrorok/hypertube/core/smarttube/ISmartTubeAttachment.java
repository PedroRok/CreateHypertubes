package com.pedrorok.hypertube.core.smarttube;

import com.pedrorok.hypertube.core.connection.interfaces.ITubeActionPoint;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/11/2025
 * @project Create Hypertube
 */
public interface ISmartTubeAttachment {
    Map<String, ISmartTubeAttachment> REGISTRY = new HashMap<>();

    String getId();
    ITubeActionPoint getActionPoint(Direction attachedDirection);

    static void register(@NotNull ISmartTubeAttachment smartTube) {
        if (REGISTRY.containsKey(smartTube.getId())) {
            throw new IllegalArgumentException("Duplicate smart tube ID: " + smartTube.getId());
        }
        REGISTRY.put(smartTube.getId(), smartTube);
    }

    @Nullable
    static ISmartTubeAttachment get(String id) {
        return REGISTRY.get(id);
    }

    static void init() {
        register(new SmartRedstoneTubeAttachment());
    }
}