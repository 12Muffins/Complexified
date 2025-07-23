package net.muffin.complexified.block.custom;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public abstract class MachineBlock extends HorizontalKineticBlock implements IWrenchable {
    public MachineBlock(BlockBehaviour.Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HORIZONTAL_FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    public static @NotNull VoxelShape genVoxelShape(VoxelShape voxelShapeNorth, @NotNull Direction direction) {
        double minX = voxelShapeNorth.bounds().minX;
        double minY = voxelShapeNorth.bounds().minY;
        double minZ = voxelShapeNorth.bounds().minZ;
        double maxX = voxelShapeNorth.bounds().maxX;
        double maxY = voxelShapeNorth.bounds().maxY;
        double maxZ = voxelShapeNorth.bounds().maxZ;

        return switch (direction) {
            case SOUTH -> Block.box((1D-maxX)*16, minY*16, (1D-maxZ)*16, (1D-minX)*16, maxY*16, (1D-minZ)*16);
            case EAST  -> Block.box((1D-maxZ)*16, minY*16, minX*16, (1D-minZ)*16, maxY*16, maxX*16);
            case WEST  -> Block.box(minZ*16, minY*16, (1D-maxX)*16, maxZ*16, maxY*16, (1D-minX)*16);
            default    -> voxelShapeNorth;
        };
    }

    /// Use of this function signature is preferred when creating the VoxelShape instead of passing a new VoxelShape
    public static @NotNull VoxelShape genVoxelShape(double pX1, double pY1, double pZ1, double pX2, double pY2, double pZ2, @NotNull Direction direction) {

        double minX = Math.min(pX1, pX2);
        double minY = Math.min(pY1, pY2);
        double minZ = Math.min(pZ1, pZ2);
        double maxX = Math.max(pX1, pX2);
        double maxY = Math.max(pY1, pY2);
        double maxZ = Math.max(pZ1, pZ2);

        return switch (direction) {
            case SOUTH -> Block.box(16D-maxX, minY, 16D-maxZ, 16D-minX, maxY, 16D-minZ);
            case EAST  -> Block.box(16D-maxZ, minY, minX, 16D-minZ, maxY, maxX);
            case WEST  -> Block.box(minZ, minY, 16D-maxX, maxZ, maxY, 16D-minX);
            default    -> Block.box(minX, minY, minZ, maxX, maxY, maxZ);
        };
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

}

