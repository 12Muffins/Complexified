package net.muffin.complexified.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;

public class SealedMachineBlock extends MachineBlock {
    public static final VoxelShape BASE = genVoxelShape(0D, 0D, 2D, 16D, 4D, 16D, Direction.NORTH);
    public static final VoxelShape DRAIN = genVoxelShape(2D, 2D, 0D, 16D, 10D, 15D, Direction.NORTH);
    public static final VoxelShape TANK = genVoxelShape(6D, 4D, 6D, 16D, 16D, 16D, Direction.NORTH);
    public static final VoxelShape PIPE = genVoxelShape(1D, 4D, 7D, 7D, 16D, 15D, Direction.NORTH);
    public static final VoxelShape[] SHAPES_DIRECTION = makeShapes();

    public SealedMachineBlock(Properties properties) {
        super(properties);
    }
    private static VoxelShape[] makeShapes() {
        return Arrays.stream(Direction.values()).map(SealedMachineBlock::calculateShape).toArray(VoxelShape[]::new);
    }

    private static VoxelShape calculateShape(Direction pDirection) {
        return switch (pDirection) {
            case SOUTH -> Shapes.or(genVoxelShape(BASE, Direction.SOUTH), genVoxelShape(DRAIN, Direction.SOUTH), genVoxelShape(TANK, Direction.SOUTH), genVoxelShape(PIPE, Direction.SOUTH));
            case WEST -> Shapes.or(genVoxelShape(BASE, Direction.WEST), genVoxelShape(DRAIN, Direction.WEST), genVoxelShape(TANK, Direction.WEST), genVoxelShape(PIPE, Direction.WEST));
            case EAST -> Shapes.or(genVoxelShape(BASE, Direction.EAST), genVoxelShape(DRAIN, Direction.EAST), genVoxelShape(TANK, Direction.EAST), genVoxelShape(PIPE, Direction.EAST));
            default -> Shapes.or(BASE, DRAIN, TANK, PIPE);
        };
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return (SHAPES_DIRECTION)[pState.getValue(HORIZONTAL_FACING).ordinal()];
    }

}
