package net.muffin.complexified.block.entity;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.LinkWithBulbBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.muffin.complexified.block.custom.AndesiteMachineBlock;
import net.muffin.complexified.block.custom.SturdyMachineBlock;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

public class SturdyMachineBlockEntity extends LinkWithBulbBlockEntity {
    public int refreshTicks;
    private static final Vec3 bulbOffset = VecHelper.voxelSpace(5, 6, 11);
    private static final Vec3[] bulbOffsetArray = genBulbOffset();
//    public static final Logger LOGGER = LogUtils.getLogger();

    public SturdyMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private static Vec3[] genBulbOffset() {
        return Arrays.stream(Direction.values()).map(SturdyMachineBlockEntity::calculateBulbPos).toArray(Vec3[]::new);
    }

    public static @NotNull Vec3 genBulbRotation(Vec3 bulbOffsetNorth, @NotNull Direction direction) {
        double px = bulbOffsetNorth.x;
        double py = bulbOffsetNorth.y;
        double pz = bulbOffsetNorth.z;
        return switch (direction) {
            case SOUTH -> VecHelper.voxelSpace((1D-px)*16, py*16, (1D-pz)*16);
            case EAST  -> VecHelper.voxelSpace((1D-pz)*16, py*16, px*16);
            case WEST  -> VecHelper.voxelSpace(pz*16, py*16, (1D-px)*16);
            default    -> bulbOffsetNorth;
        };
    }


    private static Vec3 calculateBulbPos(Direction pDirection) {
        return switch (pDirection) {
            case SOUTH -> genBulbRotation(bulbOffset, Direction.SOUTH);
            case WEST -> genBulbRotation(bulbOffset, Direction.WEST);
            case EAST -> genBulbRotation(bulbOffset, Direction.EAST);
            default -> bulbOffset;
        };
    }


    public Vec3 getBulbOffset(BlockState state) {
        return (bulbOffsetArray)[state.getValue(SturdyMachineBlock.HORIZONTAL_FACING).ordinal()];
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        writeGatheredData(tag);
        super.write(tag, clientPacket);
    }

    @Override
    public void writeSafe(CompoundTag tag) {
        writeGatheredData(tag);
        super.writeSafe(tag);
    }


    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        refreshTicks = tag.getInt("LastGlow");
        super.read(tag, clientPacket);
    }

    private void writeGatheredData(CompoundTag tag) {
        tag.putInt("LastGlow", (refreshTicks));
    }

    static CompoundTag writeIntegerTag(int integer) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putInt("ticks", integer);
        return compoundtag;
    }



    public void tick() {
        super.tick();

//        LOGGER.info("Called tick().");
        if (isVirtual())
            return;
        if (level.isClientSide)
            return;
//        LOGGER.info("Called tick() and passed first conditions.");
        refreshTicks++;
        if (refreshTicks < 40)
            return;
        refreshTicks = 0;
        tickBulb();
    }

    public void tickBulb() {
        notifyUpdate();
        sendPulseNextSync();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public Direction getBulbFacing(BlockState state) {
        return Direction.UP;
//        return state.getValue(SturdyMachineBlock.HORIZONTAL_FACING);
    }
}
