package net.muffin.complexified.block.custom;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraft.world.level.block.Block;


import net.muffin.complexified.fluid.ModFluids;
import org.slf4j.Logger;

import com.simibubi.create.content.processing.basin.BasinBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import static com.simibubi.create.AllBlocks.BASIN;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;

import net.createmod.catnip.data.Iterate;

import java.util.Arrays;

public class ResinExtractorBlock extends MachineBlock {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final FluidStack RESIN = new FluidStack(ModFluids.RESIN.get(), 20);
    private static final int maxRootDistance = 2;

    public static final VoxelShape PIPE_BEND = genVoxelShape(4D, 4D, 4D, 12D, 12D, 16D, Direction.NORTH);
    public static final VoxelShape BASE = Block.box(2D,0D,2D,14D,2D,14D);
    public static final VoxelShape PIPE_STRAIGHT = Block.box(4D,0D,4D,12D,12D,12D);
    public static final VoxelShape[] SHAPES_DIRECTION = makeShapes();

    public ResinExtractorBlock(Properties pProperties) { super(pProperties); }

    private static VoxelShape[] makeShapes() {
        return Arrays.stream(Direction.values()).map(ResinExtractorBlock::calculateShape).toArray(VoxelShape[]::new);
    }

    private static VoxelShape calculateShape(Direction pDirection) {
        return switch (pDirection) {
            case SOUTH -> Shapes.or(genVoxelShape(PIPE_BEND, Direction.SOUTH), BASE, PIPE_STRAIGHT);
            case WEST -> Shapes.or(genVoxelShape(PIPE_BEND, Direction.WEST), BASE, PIPE_STRAIGHT);
            case EAST -> Shapes.or(genVoxelShape(PIPE_BEND, Direction.EAST), BASE, PIPE_STRAIGHT);
            default -> Shapes.or(PIPE_BEND, BASE, PIPE_STRAIGHT);
        };
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return (SHAPES_DIRECTION)[pState.getValue(HORIZONTAL_FACING).ordinal()];
    }

    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (worldIn.isClientSide)
            return;

        if (!canSurvive(state, worldIn, pos)) {
                worldIn.destroyBlock(pos, true);
                return;
        }
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context);
    }

    @Override
    public boolean isRandomlyTicking(BlockState pState) { return true; }

    @Override
    public void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {

        final BlockPos basinPos = pos.below();
        if(!level.getBlockState(basinPos).is(BASIN.get())) return;

        if(!isActive(blockState, level, pos)) return;

        if (random.nextInt()%2 == 0) {
            if (!(level.getBlockState(basinPos).getBlock() instanceof BasinBlock))
                return;

            BasinBlockEntity be = BASIN.get().getBlockEntity(level, basinPos);

            IFluidHandler targetTank = be == null ? null
                    : be.getCapability(ForgeCapabilities.FLUID_HANDLER)
                    .orElse(null);

            LOGGER.info("BasinBlockEntity detection successful.");

            boolean update = false;
            for (boolean simulate : Iterate.trueAndFalse) {
                IFluidHandler.FluidAction action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
                LOGGER.info("At FluidStack check.");
                int fill = targetTank instanceof SmartFluidTankBehaviour.InternalFluidHandler
                        ? ((SmartFluidTankBehaviour.InternalFluidHandler) targetTank).forceFill(RESIN.copy(), action)
                        : targetTank.fill(RESIN.copy(), action);
                if (fill != RESIN.getAmount())
                    break;
                if (simulate)
                    continue;

                update = true;
//                if (be.visualizedOutputFluids.size() < 3)
//                    be.visualizedOutputFluids.add(IntAttached.withZero(RESIN));
            }

            LOGGER.info("Update: "+ update);
            if (update) {
                be.notifyChangeOfContents();
                be.sendData();
            }
            LOGGER.info("Should be filled");
        }
    }

    public static boolean isActive(BlockState blockState, ServerLevel level, BlockPos pos) {
        BlockPos treePos = switch (blockState.getValue(HORIZONTAL_FACING)) {
            case SOUTH -> pos.north();
            case WEST ->  pos.east();
            case EAST ->  pos.west();
            default ->    pos.south();
        };

        BlockState logState = level.getBlockState(treePos);
        if(!logState.is(Blocks.SPRUCE_LOG)) return false;

        // Check if the tree has roots
        boolean hasRoots = false;
        for (int d = 1; d <= maxRootDistance; d++) {

            for (int i = 0; i < d-1; i++) {
                if(level.getBlockState(treePos.below(d-1)) != logState) return false;
            }

            hasRoots = level.getBlockState(treePos.below(d)).is(BlockTags.DIRT);
            if(hasRoots) break;
        }
        if(!hasRoots) return false;

        int leafNum = 0;
        for (int h = 0; h < 16; h++) {
            treePos = treePos.above();
            if(level.getBlockState(treePos) != logState) return false;

            leafNum += countLeaves(level,treePos);
            if (leafNum >= 5) return true;
        }

        return false;
    }

    private static int countLeaves(ServerLevel level, BlockPos treePos) {
        int leafNum = 0;
        Direction direction = Direction.NORTH;
        for (int i = 0; i < 4; i++) {
            direction = direction.getClockWise();
            if(checkLeaves(level, treePos.relative(direction, 1))) leafNum++;
        }
        return leafNum;
    }

    private static boolean checkLeaves(ServerLevel level, BlockPos leavePos) {
        BlockState leave = level.getBlockState(leavePos);
        if(!(leave.getBlock() instanceof LeavesBlock)) return false;
        return !leave.getValue(BlockStateProperties.PERSISTENT);
    }

    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return (worldIn.getBlockState(pos.below()).getBlock() instanceof BasinBlock);
    }
}
