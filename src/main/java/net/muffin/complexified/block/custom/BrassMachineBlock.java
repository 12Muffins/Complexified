package net.muffin.complexified.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BrassMachineBlock extends MachineBlock {
    private static final VoxelShape BASE = BrassMachineBlock.box(0D, 0D, 0D, 16D, 4D, 16D);
    private static final VoxelShape TOP = BrassMachineBlock.box(1D, 0D, 1D, 15D, 13D, 15D);


    public BrassMachineBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.or(BASE, TOP);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }
}
