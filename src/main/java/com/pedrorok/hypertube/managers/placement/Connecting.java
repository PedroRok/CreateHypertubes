package com.pedrorok.hypertube.managers.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;

public record Connecting(BlockPos pos, Direction direction) {
    public static final Codec<Connecting> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(Connecting::pos),
            Direction.CODEC.fieldOf("direction").forGetter(Connecting::direction)
    ).apply(i, Connecting::new));

    public static final StreamCodec<ByteBuf, Connecting> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, Connecting::pos,
            Direction.STREAM_CODEC, Connecting::direction,
            Connecting::new
    );
}
