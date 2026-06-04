package com.pedrorok.hypertube.core.compat.sable;

import com.pedrorok.hypertube.core.compat.sable.EntityForceTracking;
import com.pedrorok.hypertube.HypertubeMod;
import com.mojang.datafixers.util.Pair;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.entity.EntitySubLevelUtil;
import dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.EntityStickExtension;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.EntityMovementExtension;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SableCompat {
    public static Pair<Vec3, Vec3> transformToWorld(Level level, Vec3 pos, Vec3 dir, boolean useLastPose) {
        final SubLevel subLevel = Sable.HELPER.getContaining(level, pos);
        if (subLevel != null) {
            return Pair.of(
                (useLastPose ? subLevel.lastPose() : subLevel.logicalPose()).transformPosition(pos),
                (useLastPose ? subLevel.lastPose() : subLevel.logicalPose()).transformNormal(dir)
            );
        }
        return Pair.of(pos, dir);
    }

    public static Pair<Vec3, Vec3> transformToWorld(Level level, Vec3 pos, Vec3 dir) {
        return transformToWorld(level, pos, dir, false);
    }

    public static Vec3 transformToWorld(Level level, Vec3 pos, boolean useLastPose) {
        return transformToWorld(level, pos, Vec3.ZERO, useLastPose).getFirst();
    }

    public static Vec3 transformToWorld(Level level, Vec3 pos) {
        return transformToWorld(level, pos, false);
    }

    public static Pair<Vec3, Vec3> transformToSubLevel(Level level, Vec3 sublevelPos, Vec3 pos, Vec3 dir, boolean useLastPose) {
        final SubLevel subLevel = Sable.HELPER.getContaining(level, sublevelPos);
        if (subLevel != null) {
            return Pair.of(
                (useLastPose ? subLevel.lastPose() : subLevel.logicalPose()).transformPositionInverse(pos),
                (useLastPose ? subLevel.lastPose() : subLevel.logicalPose()).transformNormalInverse(dir)
            );
        }
        return Pair.of(pos, dir);
    }

    public static Pair<Vec3, Vec3> transformToSubLevel(Level level, Vec3 sublevelPos, Vec3 pos, Vec3 dir) {
        return transformToSubLevel(level, sublevelPos, pos, dir, false);
    }

    public static Vec3 transformToSubLevel(Level level, Vec3 sublevelPos, Vec3 pos, boolean useLastPose) {
        return transformToSubLevel(level, sublevelPos, pos, Vec3.ZERO, useLastPose).getFirst();
    }

    public static Vec3 transformToSubLevel(Level level, Vec3 sublevelPos, Vec3 pos) {
        return transformToSubLevel(level, sublevelPos, pos, false);
    }
    
    public static void stickToSubLevel(Entity entity, Vec3 pos) {
        if (entity instanceof final EntityForceTracking trackEntity) {
            if (pos != null) {
                final SubLevel subLevel = Sable.HELPER.getContaining(entity.level(), pos);
                if (subLevel != null) {
                    trackEntity.createHypertube$setForceTrackSubLevel(subLevel);
                    return;
                }
            }
            trackEntity.createHypertube$setForceTrackSubLevel(null);
        }
    }
    
    public static class Client {
        public static Pair<Vec3, Vec3> transformToWorld(Vec3 pos, Vec3 dir, boolean useLastPose) {
            final SubLevel subLevel = Sable.HELPER.getContainingClient(pos);
            if (subLevel != null) {
                return Pair.of(
                    (useLastPose ? subLevel.lastPose() : subLevel.logicalPose()).transformPosition(pos),
                    (useLastPose ? subLevel.lastPose() : subLevel.logicalPose()).transformNormal(dir)
                );
            }
            return Pair.of(pos, dir);
        }

        public static Pair<Vec3, Vec3> transformToWorld(Vec3 pos, Vec3 dir) {
            return transformToWorld(pos, dir, false);
        }

        public static Vec3 transformToWorld(Vec3 pos, boolean useLastPose) {
            return transformToWorld(pos, Vec3.ZERO, useLastPose).getFirst();
        }

        public static Vec3 transformToWorld(Vec3 pos) {
            return transformToWorld(pos, false);
        }

        public static Pair<Vec3, Vec3> transformToSubLevel(Vec3 sublevelPos, Vec3 pos, Vec3 dir, boolean useLastPose) {
            final SubLevel subLevel = Sable.HELPER.getContainingClient(sublevelPos);
            if (subLevel != null) {
                return Pair.of(
                    (useLastPose ? subLevel.lastPose() : subLevel.logicalPose()).transformPositionInverse(pos),
                    (useLastPose ? subLevel.lastPose() : subLevel.logicalPose()).transformNormalInverse(dir)
                );
            }
            return Pair.of(pos, dir);
        }

        public static Pair<Vec3, Vec3> transformToSubLevel(Vec3 sublevelPos, Vec3 pos, Vec3 dir) {
            return transformToSubLevel(sublevelPos, pos, dir, false);
        }

        public static Vec3 transformToSubLevel(Vec3 sublevelPos, Vec3 pos, boolean useLastPose) {
            return transformToSubLevel(sublevelPos, pos, Vec3.ZERO, useLastPose).getFirst();
        }

        public static Vec3 transformToSubLevel(Vec3 sublevelPos, Vec3 pos) {
            return transformToSubLevel(sublevelPos, pos, false);
        }
    }
}