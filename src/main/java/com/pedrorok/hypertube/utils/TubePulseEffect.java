package com.pedrorok.hypertube.utils;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;

/**
 * @author Rok, Pedro Lucas nmm. 17/06/2026
 * @project Create Hypertube
 */
@Getter
@OnlyIn(Dist.CLIENT)
public class TubePulseEffect {

    private final BlockPos originBlockPos;
    private final List<Vec3> relativePoints;

    private final int ringCount;
    private final float ringSpacing;
    private final float speed;
    private final int color;
    private final float fadeOutDistance;

    private float travelDistance;
    private boolean finished;

    public TubePulseEffect(BlockPos originBlockPos, List<Vec3> relativePoints, int ringCount,
                           float ringSpacing, float speed, int color, float fadeOutDistance) {
        this.originBlockPos = originBlockPos;
        this.relativePoints = relativePoints;
        this.ringCount = ringCount;
        this.ringSpacing = ringSpacing;
        this.speed = speed;
        this.color = color;
        this.travelDistance = 0f;
        this.finished = relativePoints.size() < 2;
        this.fadeOutDistance = fadeOutDistance;
    }

    public void tick(float partialTick) {
        if (finished) return;

        travelDistance += speed * partialTick;

        float totalLength = getPathLength();
        float lastRingHead = travelDistance - (ringCount - 1) * ringSpacing;

        if (lastRingHead - ringSpacing > totalLength) {
            finished = true;
        }
    }

    public float getPathLength() {
        float length = 0f;
        for (int i = 1; i < relativePoints.size(); i++) {
            length += (float) relativePoints.get(i - 1).distanceTo(relativePoints.get(i));
        }
        return length;
    }

    public int getColorFromProgress() {
        // mistura a cor definida com a cor branca quanto mais perto do final
        float reachProgress = getReachProgress();
        int r = (int) (((color >> 16) & 0xFF) * (1 - reachProgress) + 255 * reachProgress);
        int g = (int) (((color >> 8) & 0xFF) * (1 - reachProgress) + 255 * reachProgress);
        int b = (int) ((color & 0xFF) * (1 - reachProgress) + 255 * reachProgress);
        return (r << 16) | (g << 8) | b;
    }

    public int getOpacity() {
        float reachProgress = getReachProgress();
        if (reachProgress >= 1.0f) {
            finished = true;
            return 1;
        }
        return (int) ((1.0f - reachProgress) * 255);
    }

    public float getReachProgress() {
        float fadeOutStartDist = fadeOutDistance + (ringCount - 1) * ringSpacing;
        return Mth.clamp(travelDistance / fadeOutStartDist, 0.0f, 1.0f);
    }
}