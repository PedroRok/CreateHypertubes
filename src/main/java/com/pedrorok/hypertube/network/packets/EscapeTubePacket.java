package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.travel.TravelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rok, Pedro Lucas nmm. 29/06/2026
 * @project Create Hypertube
 */
public record EscapeTubePacket() implements CustomPacketPayload {


    public static final Type<EscapeTubePacket> TYPE = new Type<>(
            HypertubeMod.of("escape_tube_packet")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EscapeTubePacket> STREAM_CODEC =
            StreamCodec.of(EscapeTubePacket::encode, EscapeTubePacket::decode);


    public static void encode(FriendlyByteBuf buf, EscapeTubePacket packet) {}

    public static EscapeTubePacket decode(FriendlyByteBuf buf) {
        return new EscapeTubePacket();
    }

    public static void handle(EscapeTubePacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            TravelManager.finishTravel((ServerPlayer) ctx.player());
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
