package net.muffin.complexified.block.custom;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.muffin.complexified.block.entity.ModBlockEntities;
import net.muffin.complexified.block.entity.SturdyMachineBlockEntity;

public class SturdyMachineBlock extends MachineBlock implements IBE<SturdyMachineBlockEntity> {
    private static final VoxelShape SHAPE_MAIN = Block.box(1D, 1D, 1D, 15D, 5D, 15D);
    private static final VoxelShape SHAPE_BASE = Block.box(2D, 0D, 2D, 14D, 1D, 14D);


    public SturdyMachineBlock(Properties pProperties) { super(pProperties); }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.or(SHAPE_MAIN, SHAPE_BASE);
    }

    @Override
    public Class<SturdyMachineBlockEntity> getBlockEntityClass() { return SturdyMachineBlockEntity.class; }

    @Override
    public BlockEntityType<? extends SturdyMachineBlockEntity> getBlockEntityType() {
        return ModBlockEntities.STURDY_MACHINE.get();
    }
}
