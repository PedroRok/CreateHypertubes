package com.pedrorok.hypertube.blocks;

import com.pedrorok.hypertube.managers.placement.BezierConnection;
import net.minecraft.core.BlockPos;

/**
 * Interface para BlockEntities que fornecem uma conexão Bezier
 */
public interface IBezierProvider {
    /**
     * Obtém a conexão Bezier associada a este BlockEntity
     */
    BezierConnection getBezierConnection();
    
    /**
     * Obtém a posição do bloco
     */
    BlockPos getBlockPos();
}