package com.pedrorok.hypertube.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/05/2025
 * @project arkanis_lore
 */
public interface Packet<T extends Packet<T>> {
    void toBytes(FriendlyByteBuf buf);
}

public interface ClientBoundPacket {
    void executeOnClient();
}

public interface ServerBoundPacket {
    void executeOnServer(net.minecraft.server.level.ServerPlayer player);
}
