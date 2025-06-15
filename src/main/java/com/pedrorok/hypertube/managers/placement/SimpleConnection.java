package com.pedrorok.hypertube.managers.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;

public record SimpleConnection(BlockPos pos, Direction direction) {
    public static final Codec<SimpleConnection> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(SimpleConnection::pos),
            Direction.CODEC.fieldOf("direction").forGetter(SimpleConnection::direction)
    ).apply(i, SimpleConnection::new));

    public static final StreamCodec<ByteBuf, SimpleConnection> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SimpleConnection::pos,
            Direction.STREAM_CODEC, SimpleConnection::direction,
            SimpleConnection::new
    );
}
