package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.managers.travel.TravelData;
import com.pedrorok.hypertube.managers.travel.TravelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Rok, Pedro Lucas nmm. Created on 18/06/2025
 * @project Create Hypertube
 */
public record TravelPointPacket(UUID player, int index) implements CustomPacketPayload {

    public static final Type<TravelPointPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "travel_index")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TravelPointPacket> STREAM_CODEC =
            StreamCodec.of(TravelPointPacket::encode, TravelPointPacket::decode);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, TravelPointPacket packet) {
        buf.writeUUID(packet.player);
        buf.writeInt(packet.index);
    }

    public static TravelPointPacket decode(RegistryFriendlyByteBuf buf) {
        return new TravelPointPacket(buf.readUUID(), buf.readInt());
    }

    public static void handleServer(TravelPointPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            //TravelManager.setIndex(packet.player, packet.index);
        });
    }
}