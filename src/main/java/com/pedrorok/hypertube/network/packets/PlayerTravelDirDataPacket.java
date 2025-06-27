package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.camera.DetachedPlayerDirController;
import com.pedrorok.hypertube.network.Packet;
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
public record PlayerTravelDirDataPacket(float yaw, float pitch) implements Packet<PlayerTravelDirDataPacket> {


    public PlayerTravelDirDataPacket(FriendlyByteBuf buf) {
        this(
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

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PlayerTravelDirDataPacket packet) {
        try {
            DetachedPlayerDirController.get().setDetached(true);
            DetachedPlayerDirController.get().updateRotation(packet.yaw, packet.pitch);
        } catch (Exception e) {
            HypertubeMod.LOGGER.error("Failed to handle PlayerTravelDirDataPacket", e);
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
    }

    @Override
    public void execute(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(this);
        });
        ctx.get().setPacketHandled(true);
    }
}
