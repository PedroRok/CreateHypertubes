package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.network.ClientBoundPacket;
import com.pedrorok.hypertube.network.Packet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

/**
 * @author Rok, Pedro Lucas nmm. Created on 18/06/2025
 * @project Create Hypertube
 */
public record  EntityTravelDirDataPacket(int entityId, float yaw,
                                        float pitch) implements Packet<EntityTravelDirDataPacket>, ClientBoundPacket {

    public EntityTravelDirDataPacket(FriendlyByteBuf buf) {
        this(
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

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void executeOnClient() {
        if (Minecraft.getInstance().player.getId() == this.entityId) return;
        Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
        if (entity == null) return;
        if (!entity.isAlive()) return;
        entity.setYRot(this.yaw);
        entity.setXRot(this.pitch);
    }
}
