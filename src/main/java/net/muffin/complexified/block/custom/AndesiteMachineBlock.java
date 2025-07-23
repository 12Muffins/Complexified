package net.muffin.complexified.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;

public class AndesiteMachineBlock extends MachineBlock {
    private static final VoxelShape SHAPE_NORTH = genVoxelShape(2D, 0D, 6D, 14D, 13D, 16D, Direction.NORTH);
    private static final VoxelShape SHAPE_EAST = genVoxelShape(SHAPE_NORTH, Direction.EAST);
    private static final VoxelShape SHAPE_SOUTH = genVoxelShape(SHAPE_NORTH,Direction.SOUTH);
    private static final VoxelShape SHAPE_WEST = genVoxelShape(SHAPE_NORTH,Direction.WEST);
    private static final VoxelShape[] SHAPES_DIRECTION = makeShapes();


    public AndesiteMachineBlock(Properties properties) {
        super(properties);
    }

    private static VoxelShape[] makeShapes() {
        return Arrays.stream(Direction.values()).map(AndesiteMachineBlock::calculateShape).toArray(VoxelShape[]::new);
    }

    private static VoxelShape calculateShape(Direction pDirection) {
        return switch (pDirection) {
            case SOUTH -> Shapes.or(SHAPE_SOUTH);
            case WEST -> Shapes.or(SHAPE_WEST);
            case EAST -> Shapes.or(SHAPE_EAST);
            default -> Shapes.or(SHAPE_NORTH);
        };
    }


    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return (SHAPES_DIRECTION)[pState.getValue(HORIZONTAL_FACING).ordinal()];
    }
}

// SHAPE_EAST (6D, 0D, 2D, 16D, 13D, 14D)
// SHAPE_WEST (0D, 0D, 2D, 10D, 13D, 14D)
// SHAPE_SOUTH (2D, 0D, 6D, 14D, 13D, 16D)
// SHAPE_NORTH (2D, 0D, 0D, 14D, 13D, 10D)