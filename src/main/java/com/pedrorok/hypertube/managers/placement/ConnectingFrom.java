package com.pedrorok.hypertube.managers.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;

public record ConnectingFrom(BlockPos pos, Direction direction) {
    public static final Codec<ConnectingFrom> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(ConnectingFrom::pos),
            Direction.CODEC.fieldOf("direction").forGetter(ConnectingFrom::direction)
    ).apply(i, ConnectingFrom::new));

    public static final StreamCodec<ByteBuf, ConnectingFrom> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ConnectingFrom::pos,
            Direction.STREAM_CODEC, ConnectingFrom::direction,
            ConnectingFrom::new
    );
}
