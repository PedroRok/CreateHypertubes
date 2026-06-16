package com.pedrorok.hypertube.utils;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.Direction;

import java.util.function.Function;


@RequiredArgsConstructor
public enum MoveDirection {
    FRONT(dir -> dir),
    LEFT(Direction::getClockWise),
    RIGHT(Direction::getCounterClockWise),
    NONE(dir -> dir);

    private final Function<Direction, Direction> directionMapper;

    public Direction map(Direction direction) {
        return directionMapper.apply(direction);
    }
}