package com.pedrorok.hypertube.network;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.network.packets.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * @author Rok, Pedro Lucas nmm. Created on 18/06/2025
 * @project Create Hypertube
 */
public class NetworkHandler {

    public static SimpleChannel INSTANCE;
    private static String PROTOCOL_VERSION;

    private static int id = 0;

    public static void init() {
        PROTOCOL_VERSION = ModList.get().getModFileById(HypertubeMod.MOD_ID).versionString();

        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(HypertubeMod.MOD_ID, "connections"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

        register(SyncPersistentDataPacket.class);
        register(FinishPathPacket.class);
        register(MovePathPacket.class);
        register(SyncEntityPosPacket.class);
        register(EntityTravelDirDataPacket.class);
        register(SpeedChangePacket.class);
        register(ActionPointReachPacket.class);
    }

    private static <T extends Packet<T>> void register(Class<T> clazz) {
        INSTANCE.registerMessage(
                id++,
                clazz,
                Packet::toBytes,
                friendlyByteBuf -> {
                    try {
                        return clazz.getConstructor(FriendlyByteBuf.class).newInstance(friendlyByteBuf);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                Packet::execute
        );
    }
}
