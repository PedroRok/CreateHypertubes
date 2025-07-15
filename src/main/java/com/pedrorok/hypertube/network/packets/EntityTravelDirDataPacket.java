package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.network.Packet;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 18/06/2025
 * @project Create Hypertube
 */
public record EntityTravelDirDataPacket(int entityId, float yaw,
                                        float pitch) implements Packet<EntityTravelDirDataPacket> {

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
    }

    public EntityTravelDirDataPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readFloat(), buf.readFloat());
    }

    public static EntityTravelDirDataPacket create(Entity entity) {
        return new EntityTravelDirDataPacket(
                entity.getId(),
                entity.getYRot(),
                entity.getXRot()
        );
    }

    @Override
    public void execute(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(this);
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(EntityTravelDirDataPacket packet) {
        if (Minecraft.getInstance().player.getId() == packet.entityId) return;
        Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId);
        if (entity == null) return;
        if (!entity.isAlive()) return;
        entity.setYRot(packet.yaw);
        entity.setXRot(packet.pitch);
    }
}
