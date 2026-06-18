package com.pedrorok.hypertube.utils;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;


@RequiredArgsConstructor
public enum MoveDirection implements StringRepresentable {
    FRONT(dir -> dir),
    LEFT(Direction::getCounterClockWise),
    RIGHT(Direction::getClockWise),
    NONE(dir -> dir);

    private final Function<Direction, Direction> directionMapper;

    public Direction map(Direction direction) {
        return directionMapper.apply(direction);
    }

    public static MoveDirection fromDirections(Direction from, Direction to) {
        for (MoveDirection moveDirection : values()) {
            if (moveDirection.map(from) == to) {
                return moveDirection;
            }
        }
        return NONE;
    }

    @Override
    public @NotNull String getSerializedName() {
        return switch (this) {
            case FRONT -> "front";
            case LEFT -> "left";
            case RIGHT -> "right";
            case NONE -> "none";
        };
    }
}