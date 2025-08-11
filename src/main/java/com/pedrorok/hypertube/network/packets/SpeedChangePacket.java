package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.travel.ClientTravelPathMover;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rok, Pedro Lucas nmm. Created on 09/08/2025
 * @project Create Hypertube
 */
public record SpeedChangePacket(int entityId, double newSpeed) implements CustomPacketPayload {

    public static final Type<SpeedChangePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "entity_travel_speed_change")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SpeedChangePacket> STREAM_CODEC =
            StreamCodec.of(SpeedChangePacket::encode, SpeedChangePacket::decode);


    public static void encode(FriendlyByteBuf buf, SpeedChangePacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeDouble(packet.newSpeed);
    }

    public static SpeedChangePacket decode(FriendlyByteBuf buf) {
        int id = buf.readInt();
        double speed = buf.readDouble();
        return new SpeedChangePacket(id, speed);
    }

    public static void handle(SpeedChangePacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientTravelPathMover.updateEntitySpeed(packet);
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}