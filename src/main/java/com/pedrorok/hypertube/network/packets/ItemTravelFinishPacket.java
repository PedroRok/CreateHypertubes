package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.travel.ClientItemTravelPathMover;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ItemTravelFinishPacket(UUID itemUuid) implements CustomPacketPayload {

    public static final Type<ItemTravelFinishPacket> TYPE = new Type<>(
            HypertubeMod.of("item_travel_finish")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemTravelFinishPacket> STREAM_CODEC =
            StreamCodec.of(ItemTravelFinishPacket::encode, ItemTravelFinishPacket::decode);

    public static void encode(RegistryFriendlyByteBuf buf, ItemTravelFinishPacket packet) {
        buf.writeUUID(packet.itemUuid);
    }

    public static ItemTravelFinishPacket decode(RegistryFriendlyByteBuf buf) {
        return new ItemTravelFinishPacket(buf.readUUID());
    }

    public static void handle(ItemTravelFinishPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientItemTravelPathMover.finishTravel(packet.itemUuid);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
