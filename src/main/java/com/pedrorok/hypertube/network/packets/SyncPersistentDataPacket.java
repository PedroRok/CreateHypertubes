package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.network.ClientBoundPacket;
import com.pedrorok.hypertube.network.Packet;
import com.simibubi.create.foundation.networking.ISyncPersistentData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import java.util.HashSet;

/**
 * @author Rok, Pedro Lucas nmm. Created on 18/06/2025
 * @project Create Hypertube
 */
public record SyncPersistentDataPacket(int entityId, CompoundTag readData) implements Packet<SyncPersistentDataPacket>, ClientBoundPacket {

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

    @Environment(EnvType.CLIENT)
    private void handleClient() {
        try {
            Entity entityByID = Minecraft.getInstance().level.getEntity(this.entityId);
            if (entityByID == null) {
                return;
            }
            CompoundTag data = entityByID.getPersistentData();
            new HashSet<>(data.getAllKeys()).forEach(data::remove);
            data.merge(this.readData);
            if (!(entityByID instanceof ISyncPersistentData))
                return;
            ((ISyncPersistentData) entityByID).onPersistentDataUpdated();
        } catch (Exception e) {
            HypertubeMod.LOGGER.error("Failed to handle SyncPersistentDataPacket for entity ID: {}", this.entityId, e);
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeNbt(readData);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void executeOnClient() {
        handleClient();
    }
}
