package net.muffin.complexified.block.custom;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.muffin.complexified.block.ModBlocks;
import net.muffin.complexified.block.entity.BoilerHeaterBlockEntityOld;
import net.muffin.complexified.block.entity.ModBlockEntities;
import org.slf4j.Logger;

public class BoilerHeaterBlockOld extends KineticBlock implements IBE<BoilerHeaterBlockEntityOld> {
    public static final Logger LOGGER = LogUtils.getLogger();


    public BoilerHeaterBlockOld(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState stateForPlacement = super.getStateForPlacement(context);
        BlockPos pos = context.getClickedPos();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1 ; z++) {
                BlockPos offset = new BlockPos(x, 0, z);
                if (offset.equals(BlockPos.ZERO))
                    continue;
                BlockState occupiedState = context.getLevel().getBlockState(pos.offset(offset));

                LOGGER.info("Block at: x, ~ z: " + pos.offset(offset));
                LOGGER.info("Block is: " + occupiedState.getBlock());

                if (!occupiedState.canBeReplaced()) {
                    LOGGER.info("Cannot be placed");
                    return null;
                }
            }
        }

        LOGGER.info("Can be placed");
        return stateForPlacement;
    }

//    @Override
//    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
//        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
//    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.getBlockTicks()
                .hasScheduledTick(pos, this))
            level.scheduleTick(pos, this, 1);
        withBlockEntityDo(level, pos, BoilerHeaterBlockEntityOld::updateConnectivity);
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        for (Direction side : Iterate.horizontalDirections) {

            for (boolean secondary : Iterate.falseAndTrue) {
                Direction targetSide = secondary ? side.getClockWise() : side;
                BlockPos structurePos = (secondary ? pPos.relative(side) : pPos).relative(targetSide);
                BlockState occupiedState = pLevel.getBlockState(structurePos);
                BlockState requiredStructure = ModBlocks.BOILER_HEATER_STRUCTURAL.getDefaultState()
                        .setValue(BoilerHeaterStructuralBlock.FACING, targetSide.getOpposite());
                if (occupiedState == requiredStructure)
                    continue;
                if (!occupiedState.canBeReplaced()) {
                    pLevel.destroyBlock(pPos, false);
                    return;
                }
                pLevel.setBlockAndUpdate(structurePos, requiredStructure);
            }
        }
    }


    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return null;
    }

    @Override
    public Class<BoilerHeaterBlockEntityOld> getBlockEntityClass() {
        return BoilerHeaterBlockEntityOld.class;
    }

    @Override
    public BlockEntityType<? extends BoilerHeaterBlockEntityOld> getBlockEntityType() {
        return ModBlockEntities.BOILER_HEATER_OLD.get();
    }

    public static float getHeat(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BoilerHeaterBlockEntityOld heater) {
            return heater.getHeatValue();
        }
        return BoilerHeater.NO_HEAT;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public static int getLightLevel(BlockState state) {
        return 15;
    }
}
