package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.travel.ClientTravelPathMover;
import com.pedrorok.hypertube.core.travel.TravelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Rok, Pedro Lucas nmm. Created on 10/08/2025
 * @project Create Hypertube
 */
public record ActionPointReachPacket(UUID entityId, BlockPos pos) implements CustomPacketPayload {

    public static final Type<ActionPointReachPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "entity_action_point_reach")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ActionPointReachPacket> STREAM_CODEC =
            StreamCodec.of(ActionPointReachPacket::encode, ActionPointReachPacket::decode);


    public static void encode(FriendlyByteBuf buf, ActionPointReachPacket packet) {
        buf.writeUUID(packet.entityId);
        buf.writeBlockPos(packet.pos);
    }

    public static ActionPointReachPacket decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        BlockPos pos = buf.readBlockPos();
        return new ActionPointReachPacket(id, pos);
    }

    public static void handle(ActionPointReachPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            TravelManager.actionPointReach(packet.entityId, packet.pos);
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
