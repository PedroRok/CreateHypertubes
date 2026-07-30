package com.pedrorok.hypertube.core.compat.sable;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SableCompat {

    public static Pair<Vec3, Vec3> transformToWorld(Level level, Vec3 pos, Vec3 dir, boolean useLastPose) {
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
    }

    public static class Client {
        public static Pair<Vec3, Vec3> transformToWorld(Vec3 pos, Vec3 dir, boolean useLastPose) {
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
