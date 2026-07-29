package com.pedrorok.hypertube.ponder.elements;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pedrorok.hypertube.core.connection.BezierConnection;
import com.pedrorok.hypertube.utils.TubePulseEffect;
import com.pedrorok.hypertube.utils.TubePulseRenderer;
import net.createmod.ponder.api.element.PonderSceneElement;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.element.PonderElementBase;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * @author Rok, Pedro Lucas nmm. 29/07/2026
 * @project Create Hypertube
 */
@OnlyIn(Dist.CLIENT)
public class TubePulsePonderElement extends PonderElementBase implements PonderSceneElement {

    private static final RenderType LINES = RenderType.create("create_hypertube_ponder_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.MAIN_TARGET)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    private static final int RING_COUNT = 4;
    private static final float RING_SPACING = 0.08f;
    private static final float SPEED = 0.2f;
    private static final float FADE_OUT_DISTANCE = 5;
    private static final float RING_RADIUS = 0.72f;
    private static final int SPAWN_INTERVAL = 10;

    private final BlockPos origin;
    private final List<Vec3> relativePoints;
    private final int color;
    private final List<TubePulseEffect> effects = new ArrayList<>();

    private int remainingTicks;
    private int spawnCooldown;

    private TubePulsePonderElement(BlockPos origin, List<Vec3> relativePoints, int color, int durationTicks) {
        this.origin = origin;
        this.relativePoints = relativePoints;
        this.color = color;
        this.remainingTicks = durationTicks;
        setVisible(true);
    }

    @Nullable
    public static TubePulsePonderElement of(BezierConnection connection, BlockPos referencePos, int color, int durationTicks) {
        BlockPos origin = connection.getFromPos().pos();
        List<Vec3> points = connection.getRelativeBezierPoints(origin);
        if (points.size() < 2) return null;
        if (connection.isInverted(referencePos)) {
            points = points.reversed();
        }
        return new TubePulsePonderElement(origin, points, color, durationTicks);
    }

    @Override
    public void tick(PonderScene scene) {
        if (remainingTicks > 0) {
            remainingTicks--;
            if (spawnCooldown <= 0) {
                spawnCooldown = SPAWN_INTERVAL;
                effects.add(new TubePulseEffect(origin, relativePoints, RING_COUNT, RING_SPACING, SPEED, color,
                        0, FADE_OUT_DISTANCE, RING_RADIUS));
            }
            spawnCooldown--;
        }
        effects.forEach(effect -> effect.tick(1));
        effects.removeIf(TubePulseEffect::isFinished);
        if (remainingTicks <= 0 && effects.isEmpty()) {
            setVisible(false);
        }
    }

    @Override
    public void reset(PonderScene scene) {
        effects.clear();
        setVisible(false);
    }

    @Override
    public void renderFirst(PonderLevel level, MultiBufferSource buffer, GuiGraphics graphics, float partialTicks) {
    }

    @Override
    public void renderLayer(PonderLevel level, MultiBufferSource buffer, RenderType type, GuiGraphics graphics, float partialTicks) {
    }

    @Override
    public void renderLast(PonderLevel level, MultiBufferSource buffer, GuiGraphics graphics, float partialTicks) {
        if (effects.isEmpty()) return;

        VertexConsumer consumer = buffer.getBuffer(LINES);
        Vec3 renderOrigin = new Vec3(origin.getX(), origin.getY(), origin.getZ());

        for (TubePulseEffect effect : effects) {
            if (effect.isFinished()) continue;
            TubePulseRenderer.renderEffectAt(effect, graphics.pose(), consumer, renderOrigin, partialTicks);
        }
    }
}
