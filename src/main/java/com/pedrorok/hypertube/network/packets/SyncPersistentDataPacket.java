package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

/**
 * @author Rok, Pedro Lucas nmm. Created on 18/06/2025
 * @project Create Hypertube
 */
public record SyncPersistentDataPacket(int entityId, CompoundTag readData) implements CustomPacketPayload {

    public static final Type<SyncPersistentDataPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(HypertubeMod.MOD_ID, "travel_index")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPersistentDataPacket> STREAM_CODEC =
            StreamCodec.of(SyncPersistentDataPacket::encode, SyncPersistentDataPacket::decode);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RegistryFriendlyByteBuf buf, SyncPersistentDataPacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeNbt(packet.readData);
    }

    public static SyncPersistentDataPacket decode(RegistryFriendlyByteBuf buf) {
        return new SyncPersistentDataPacket(
                buf.readInt(),
                buf.readNbt()
        );
    }

    public static SyncPersistentDataPacket create(Entity entity) {
        return new SyncPersistentDataPacket(
                entity.getId(),
                entity.getPersistentData()
        );
    }

    public static void handle(SyncPersistentDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            handleClient(packet);
        });
    }

    private static void handleClient(SyncPersistentDataPacket packet) {
        try {
            Entity entityByID = Minecraft.getInstance().level.getEntity(packet.entityId);
            CompoundTag data = entityByID.getPersistentData();
            new HashSet<>(data.getAllKeys()).forEach(data::remove);
            data.merge(packet.readData);
            if (!(entityByID instanceof ISyncPersistentData))
                return;
            ((ISyncPersistentData) entityByID).onPersistentDataUpdated();
        } catch (Exception e) {
            HypertubeMod.LOGGER.error("Failed to handle SyncPersistentDataPacket for entity ID: {}", packet.entityId, e);
        }
    }
}
