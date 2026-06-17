package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.travel.TravelManager;
import com.pedrorok.hypertube.utils.MoveDirection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record MoveDirectionPacket(MoveDirection direction) implements CustomPacketPayload {

    public static final Type<MoveDirectionPacket> TYPE =
            new Type<>(HypertubeMod.of("move_direction"));

    public static final StreamCodec<FriendlyByteBuf, MoveDirectionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    NeoForgeStreamCodecs.enumCodec(MoveDirection.class), MoveDirectionPacket::direction,
                    MoveDirectionPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MoveDirectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            TravelManager.changeDirection(packet.direction(), player.getUUID(), player.level());
        });
    }
}