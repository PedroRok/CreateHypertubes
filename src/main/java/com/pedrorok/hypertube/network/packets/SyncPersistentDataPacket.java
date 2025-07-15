package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.network.Packet;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 18/06/2025
 * @project Create Hypertube
 */
public record SyncPersistentDataPacket(int entityId, CompoundTag readData) implements Packet<SyncPersistentDataPacket> {

    public SyncPersistentDataPacket(FriendlyByteBuf buf) {
        this(
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

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(SyncPersistentDataPacket packet) {
        try {
            Entity entityByID = Minecraft.getInstance().level.getEntity(packet.entityId);
            if (entityByID == null) {
                return;
            }
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

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeNbt(readData);
    }

    @Override
    public void execute(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(this);
        });
        ctx.get().setPacketHandled(true);
    }
}
