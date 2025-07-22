package com.pedrorok.hypertube.core.connection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pedrorok.hypertube.core.connection.interfaces.ISimpleConnection;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;

/**
 * @author Rok, Pedro Lucas nmm. Created on 22/07/2025
 * @project Create Hypertube
 */
public record ConnDTO(BlockPos pos, Direction direction) implements ISimpleConnection<BlockPos> {

    public static final Codec<ConnDTO> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(ConnDTO::pos),
            Direction.CODEC.fieldOf("direction").forGetter(ConnDTO::direction)
    ).apply(i, ConnDTO::new));

    public static final StreamCodec<ByteBuf, ConnDTO> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ConnDTO::pos,
            Direction.STREAM_CODEC, ConnDTO::direction,
            ConnDTO::new
    );

    public BlockPos pos() {
        return pos;
    }

    public SimpleConnection toSimpleConnection(BlockPos relative) {
        //BlockPos absolutePos = pos.subtract(relative);
        return new SimpleConnection(pos, direction);
        //return new SimpleConnection(relative.subtract(absolutePos).getCenter(), direction);
    }
}
