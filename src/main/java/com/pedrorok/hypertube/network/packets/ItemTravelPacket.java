package com.pedrorok.hypertube.network.packets;

import com.pedrorok.hypertube.HypertubeMod;
import com.pedrorok.hypertube.core.travel.ClientItemTravelPathMover;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ItemTravelPacket(UUID itemUuid, ItemStack itemStack, List<Vec3> pathPoints,
                               Set<BlockPos> actionPoints, double travelSpeed) implements CustomPacketPayload {

    public static final Type<ItemTravelPacket> TYPE = new Type<>(
            HypertubeMod.of("item_travel_path")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemTravelPacket> STREAM_CODEC =
            StreamCodec.of(ItemTravelPacket::encode, ItemTravelPacket::decode);

    public static void encode(RegistryFriendlyByteBuf buf, ItemTravelPacket packet) {
        buf.writeUUID(packet.itemUuid);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, packet.itemStack);
        buf.writeInt(packet.pathPoints.size());
        for (Vec3 vec : packet.pathPoints) {
            buf.writeDouble(vec.x);
            buf.writeDouble(vec.y);
            buf.writeDouble(vec.z);
        }
        buf.writeInt(packet.actionPoints.size());
        for (BlockPos blockPos : packet.actionPoints) {
            buf.writeBlockPos(blockPos);
        }
        buf.writeDouble(packet.travelSpeed);
    }

    public static ItemTravelPacket decode(RegistryFriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        int size = buf.readInt();
        List<Vec3> points = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            points.add(new Vec3(x, y, z));
        }
        size = buf.readInt();
        Set<BlockPos> actionPoints = new HashSet<>();
        for (int i = 0; i < size; i++) {
            actionPoints.add(buf.readBlockPos());
        }
        double speed = buf.readDouble();
        return new ItemTravelPacket(uuid, stack, points, actionPoints, speed);
    }

    public static void handle(ItemTravelPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientItemTravelPathMover.startMoving(packet);
        });
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
