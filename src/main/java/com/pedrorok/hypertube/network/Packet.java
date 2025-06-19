package com.pedrorok.hypertube.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Rok, Pedro Lucas nmm. Created on 19/05/2025
 * @project arkanis_lore
 */
public interface Packet<T extends Packet<T>> {
    void toBytes(FriendlyByteBuf buf);

    void execute(Supplier<NetworkEvent.Context> ctx);
}
