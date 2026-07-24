package com.pedrorok.hypertube.blocks.blockentities;

import com.pedrorok.hypertube.registry.ModBlockEntities;
import com.pedrorok.hypertube.utils.VoxelUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rok, Pedro Lucas nmm.
 * @project Create Hypertube
 */
public class TubePathBlockEntity extends BlockEntity {

    private static final String KEY_OWNER = "owner";
    private static final String KEY_BOX = "box";

    private @Nullable BlockPos owner;
    private VoxelShape shape = Shapes.empty();

    public TubePathBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void bind(BlockPos owner, List<AABB> boxes) {
        this.owner = owner;
        this.shape = normalize(boxes);
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public @Nullable BlockPos owner() {
        return owner;
    }

    public VoxelShape shape() {
        return shape;
    }

    private static VoxelShape union(List<AABB> boxes) {
        VoxelShape shape = Shapes.empty();
        for (AABB box : boxes) {
            shape = VoxelUtils.combine(shape, Shapes.create(box));
        }
        return shape;
    }

    private static VoxelShape normalize(List<AABB> boxes) {
        List<AABB> snapped = new ArrayList<>();
        for (AABB box : union(boxes).toAabbs()) {
            snapped.add(new AABB(
                    snap(box.minX), snap(box.minY), snap(box.minZ),
                    snap(box.maxX), snap(box.maxY), snap(box.maxZ)));
        }
        return union(snapped);
    }

    private static double snap(double value) {
        return decode(encode(value));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner == null) {
            return;
        }
        BlockPos relative = owner.subtract(worldPosition);
        tag.putIntArray(KEY_OWNER, new int[]{relative.getX(), relative.getY(), relative.getZ()});

        List<AABB> boxes = shape.toAabbs();
        int[] packed = new int[boxes.size() * 6];
        for (int i = 0; i < boxes.size(); i++) {
            AABB box = boxes.get(i);
            packed[i * 6] = encode(box.minX);
            packed[i * 6 + 1] = encode(box.minY);
            packed[i * 6 + 2] = encode(box.minZ);
            packed[i * 6 + 3] = encode(box.maxX);
            packed[i * 6 + 4] = encode(box.maxY);
            packed[i * 6 + 5] = encode(box.maxZ);
        }
        tag.putIntArray(KEY_BOX, packed);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = null;
        shape = Shapes.empty();

        int[] stored = tag.getIntArray(KEY_OWNER);
        if (stored.length == 3) {
            owner = new BlockPos(stored[0], stored[1], stored[2]).offset(worldPosition);
        }

        int[] packed = tag.getIntArray(KEY_BOX);
        if (packed.length >= 6 && packed.length % 6 == 0) {
            List<AABB> boxes = new ArrayList<>(packed.length / 6);
            for (int i = 0; i < packed.length; i += 6) {
                boxes.add(new AABB(
                        decode(packed[i]), decode(packed[i + 1]), decode(packed[i + 2]),
                        decode(packed[i + 3]), decode(packed[i + 4]), decode(packed[i + 5])));
            }
            shape = union(boxes);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private static int encode(double value) {
        return (int) Math.round(value * 4096);
    }

    private static double decode(int value) {
        return value / 4096.0;
    }
}
