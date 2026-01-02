package com.pedrorok.hypertube.network;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.network.packets.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Rok, Pedro Lucas nmm. Created on 18/06/2025
 * @project Create Hypertube
 */
public class NetworkHandler {

    private static final Map<Class<? extends Packet<?>>, ResourceLocation> PACKET_IDS = new HashMap<>();
    private static final Map<ResourceLocation, Function<FriendlyByteBuf, ? extends Packet<?>>> PACKET_DECODERS = new HashMap<>();
    private static int id = 0;

    public static void init() {
        register(SyncPersistentDataPacket.class, SyncPersistentDataPacket::new);
        register(FinishPathPacket.class, FinishPathPacket::new);
        register(MovePathPacket.class, MovePathPacket::new);
        register(SyncEntityPosPacket.class, SyncEntityPosPacket::new);
        register(EntityTravelDirDataPacket.class, EntityTravelDirDataPacket::new);
        register(SpeedChangePacket.class, SpeedChangePacket::new);
        register(ActionPointReachPacket.class, ActionPointReachPacket::new);
    }

    private static <T extends Packet<T>> void register(Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        ResourceLocation packetId = new ResourceLocation(HypertubeMod.MOD_ID, "packet_" + id++);
        PACKET_IDS.put(clazz, packetId);
        PACKET_DECODERS.put(packetId, decoder);

        // Registrar no servidor (pacotes do cliente para o servidor)
        ServerPlayNetworking.registerGlobalReceiver(packetId, (server, player, handler, buf, responseSender) -> {
            T packet = decoder.apply(buf);
            server.execute(() -> {
                if (packet instanceof ServerBoundPacket) {
                    ((ServerBoundPacket) packet).executeOnServer(player);
                }
            });
        });

        // Registrar no cliente (pacotes do servidor para o cliente)
        ClientPlayNetworking.registerGlobalReceiver(packetId, (client, handler, buf, responseSender) -> {
            T packet = decoder.apply(buf);
            client.execute(() -> {
                if (packet instanceof ClientBoundPacket) {
                    ((ClientBoundPacket) packet).executeOnClient();
                }
            });
        });
    }

    public static void sendToServer(Packet<?> packet) {
        ResourceLocation id = PACKET_IDS.get(packet.getClass());
        if (id == null) {
            HypertubeMod.LOGGER.error("Tried to send unregistered packet: {}", packet.getClass());
            return;
        }
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.toBytes(buf);
        ClientPlayNetworking.send(id, buf);
    }

    public static void sendToClient(ServerPlayer player, Packet<?> packet) {
        ResourceLocation id = PACKET_IDS.get(packet.getClass());
        if (id == null) {
            HypertubeMod.LOGGER.error("Tried to send unregistered packet: {}", packet.getClass());
            return;
        }
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.toBytes(buf);
        ServerPlayNetworking.send(player, id, buf);
    }

    public static void sendToTrackingEntity(Entity entity, Packet<?> packet) {
        if (!(entity.level() instanceof Level level) || level.isClientSide) return;
        ResourceLocation id = PACKET_IDS.get(packet.getClass());
        if (id == null) {
            HypertubeMod.LOGGER.error("Tried to send unregistered packet: {}", packet.getClass());
            return;
        }
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.toBytes(buf);
        ServerPlayNetworking.sendToPlayersTrackingEntity(entity, id, buf);
    }

    public static void sendToTrackingEntityAndSelf(Entity entity, Packet<?> packet) {
        if (!(entity.level() instanceof Level level) || level.isClientSide) return;
        ResourceLocation id = PACKET_IDS.get(packet.getClass());
        if (id == null) {
            HypertubeMod.LOGGER.error("Tried to send unregistered packet: {}", packet.getClass());
            return;
        }
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.toBytes(buf);
        ServerPlayNetworking.sendToPlayersTrackingEntityAndSelf(entity, id, buf);
    }

    public static void sendToAll(Packet<?> packet) {
        ResourceLocation id = PACKET_IDS.get(packet.getClass());
        if (id == null) {
            HypertubeMod.LOGGER.error("Tried to send unregistered packet: {}", packet.getClass());
            return;
        }
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.toBytes(buf);
        // Enviar para todos os jogadores conectados
        // Isso precisa ser feito através de um servidor, então será implementado onde necessário
    }
}
