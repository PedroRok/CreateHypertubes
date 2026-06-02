package com.pedrorok.hypertube.core.compat.sable;

import com.mojang.datafixers.util.Pair;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SableCompat {
	public static Pair<Vec3, Vec3> transformToWorld(Level level, Vec3 pos, Vec3 dir) {
		final SubLevel subLevel = Sable.HELPER.getContaining(level, pos);
		if (subLevel != null) {
			return Pair.of(
				subLevel.logicalPose().transformPosition(pos),
				subLevel.logicalPose().transformNormal(dir)
			);
		}
		return Pair.of(pos, dir);
	}

	public static Vec3 transformToWorld(Level level, Vec3 pos) {
		final SubLevel subLevel = Sable.HELPER.getContaining(level, pos);
		if (subLevel != null) {
			return subLevel.logicalPose().transformPosition(pos);
		}
		return pos;
	}

	public static Pair<Vec3, Vec3> transformToSubLevel(Level level, Vec3 pos, Vec3 dir) {
		final SubLevel subLevel = Sable.HELPER.getContaining(level, pos);
		if (subLevel != null) {
			return Pair.of(
				subLevel.logicalPose().transformPositionInverse(pos),
				subLevel.logicalPose().transformNormalInverse(dir)
			);
		}
		return Pair.of(pos, dir);
	}

	public static Vec3 transformToSubLevel(Level level, Vec3 pos) {
		final SubLevel subLevel = Sable.HELPER.getContaining(level, pos);
		if (subLevel != null) {
			return subLevel.logicalPose().transformPositionInverse(pos);
		}
		return pos;
	}
	
	public static class Client {
		public static Pair<Vec3, Vec3> transformToWorld(Vec3 pos, Vec3 dir) {
			final SubLevel subLevel = Sable.HELPER.getContainingClient(pos);
			if (subLevel != null) {
				return Pair.of(
					subLevel.logicalPose().transformPosition(pos),
					subLevel.logicalPose().transformNormal(dir)
				);
			}
			return Pair.of(pos, dir);
		}

		public static Vec3 transformToWorld(Vec3 pos) {
			final SubLevel subLevel = Sable.HELPER.getContainingClient(pos);
			if (subLevel != null) {
				return subLevel.logicalPose().transformPosition(pos);
			}
			return pos;
		}

		public static Pair<Vec3, Vec3> transformToSubLevel(Vec3 pos, Vec3 dir) {
			final SubLevel subLevel = Sable.HELPER.getContainingClient(pos);
			if (subLevel != null) {
				return Pair.of(
					subLevel.logicalPose().transformPositionInverse(pos),
					subLevel.logicalPose().transformNormalInverse(dir)
				);
			}
			return Pair.of(pos, dir);
		}

		public static Vec3 transformToSubLevel(Vec3 pos) {
			final SubLevel subLevel = Sable.HELPER.getContainingClient(pos);
			if (subLevel != null) {
				return subLevel.logicalPose().transformPositionInverse(pos);
			}
			return pos;
		}
	}
}