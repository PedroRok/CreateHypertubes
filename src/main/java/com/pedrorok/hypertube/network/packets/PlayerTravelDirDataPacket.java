package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rok, Pedro Lucas nmm. Created on 18/06/2025
 * @project Create Hypertube
 */
public record PlayerTravelDirDataPacket(float yaw, float pitch) implements CustomPacketPayload {


    public static float YAW = 0.0f;
    public static float PITCH = 0.0f;

    public static final Type<PlayerTravelDirDataPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "player_travel_dir")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerTravelDirDataPacket> STREAM_CODEC =
            StreamCodec.of(PlayerTravelDirDataPacket::encode, PlayerTravelDirDataPacket::decode);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, PlayerTravelDirDataPacket packet) {
        buf.writeFloat(packet.yaw);
        buf.writeFloat(packet.pitch);
    }

    public static PlayerTravelDirDataPacket decode(RegistryFriendlyByteBuf buf) {
        return new PlayerTravelDirDataPacket(
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public static PlayerTravelDirDataPacket create(Entity entity) {
        return new PlayerTravelDirDataPacket(
                entity.getYRot(),
                entity.getXRot()
        );
    }

    public static void handle(PlayerTravelDirDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            handleClient(packet);
        });
    }

    private static void handleClient(PlayerTravelDirDataPacket packet) {
        try {
            YAW = packet.yaw;
            PITCH = packet.pitch;
        } catch (Exception e) {
            HypertubeMod.LOGGER.error("Failed to handle PlayerTravelDirDataPacket", e);
        }
    }
}
