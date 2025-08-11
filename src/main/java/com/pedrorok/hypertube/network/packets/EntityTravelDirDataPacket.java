package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rok, Pedro Lucas nmm. Created on 18/06/2025
 * @project Create Hypertube
 */
public record EntityTravelDirDataPacket(int entityId, float yaw, float pitch) implements CustomPacketPayload {

    public static final Type<EntityTravelDirDataPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "player_travel_dir")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityTravelDirDataPacket> STREAM_CODEC =
            StreamCodec.of(EntityTravelDirDataPacket::encode, EntityTravelDirDataPacket::decode);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, EntityTravelDirDataPacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeFloat(packet.yaw);
        buf.writeFloat(packet.pitch);
    }

    public static EntityTravelDirDataPacket decode(RegistryFriendlyByteBuf buf) {
        return new EntityTravelDirDataPacket(
                buf.readInt(),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public static EntityTravelDirDataPacket create(Entity entity) {
        return new EntityTravelDirDataPacket(
                entity.getId(),
                entity.getYRot(),
                entity.getXRot()
        );
    }

    public static void handle(EntityTravelDirDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            handleClient(packet);
        });
    }

    @OnlyIn(value = Dist.CLIENT)
    private static void handleClient(EntityTravelDirDataPacket packet) {
        if (Minecraft.getInstance().player.getId() == packet.entityId) return;
        Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId);
        if (entity == null) return;
        if (!entity.isAlive()) return;
        entity.setYRot(packet.yaw);
        entity.setXRot(packet.pitch);
    }
}
