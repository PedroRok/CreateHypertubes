package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.travel.TravelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 03/07/2025
 * @project Create Hypertube
 */
public record FinishPathPacket(UUID entityUuid) implements CustomPacketPayload {

    public static final Type<FinishPathPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "finish_travel_path")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FinishPathPacket> STREAM_CODEC =
            StreamCodec.of(FinishPathPacket::encode, FinishPathPacket::decode);


    public static void encode(FriendlyByteBuf buf, FinishPathPacket packet) {
        buf.writeUUID(packet.entityUuid);
    }

    public static FinishPathPacket decode(FriendlyByteBuf buf) {
        return new FinishPathPacket(buf.readUUID());
    }

    public static void handle(FinishPathPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            TravelManager.finishTravel(packet.entityUuid);
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
