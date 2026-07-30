package com.pedrorok.hypertube.ponder.elements;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.OptionalDouble;

/**
 * Holder for the ponder pulse line type. It extends {@link RenderType} only so the
 * {@code RenderStateShard} presets, which are protected on 1.20.1, can be referenced by simple name.
 *
 * @author Rok, Pedro Lucas nmm. 29/07/2026
 * @project Create Hypertube
 */
@OnlyIn(Dist.CLIENT)
abstract class HypertubePonderRenderTypes extends RenderType {

    /**
     * Same setup as {@link RenderType#lines()}, but drawn into the main target instead of the item
     * entity target so it shows up inside the ponder framebuffer.
     */
    static final RenderType PULSE_LINES = RenderType.create("create_hypertube_ponder_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setLineState(new LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(MAIN_TARGET)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    private HypertubePonderRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                       boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
}
