package net.muffin.complexified.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Pahoehoe extends ForgeFlowingFluid {
    protected Pahoehoe(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isRandomlyTicking() {
        return true;
    }

    @Override
    protected void randomTick(Level pLevel, BlockPos pPos, FluidState pState, RandomSource pRandom) {

        if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            int i = pRandom.nextInt(3);
            if (i > 0) {
                BlockPos blockpos = pPos;

                for(int j = 0; j < i; ++j) {
                    blockpos = blockpos.offset(pRandom.nextInt(3) - 1, 1, pRandom.nextInt(3) - 1);
                    if (!pLevel.isLoaded(blockpos)) {
                        return;
                    }

                    BlockState blockstate = pLevel.getBlockState(blockpos);
                    if (blockstate.isAir()) {
                        if (this.hasFlammableNeighbours(pLevel, blockpos)) {
                            pLevel.setBlockAndUpdate(blockpos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(pLevel, blockpos, pPos, Blocks.FIRE.defaultBlockState()));
                            return;
                        }
                    } else if (blockstate.blocksMotion()) {
                        return;
                    }
                }
            } else {
                for(int k = 0; k < 3; ++k) {
                    BlockPos blockpos1 = pPos.offset(pRandom.nextInt(3) - 1, 0, pRandom.nextInt(3) - 1);
                    if (!pLevel.isLoaded(blockpos1)) {
                        return;
                    }

                    if (pLevel.isEmptyBlock(blockpos1.above()) && this.isFlammable(pLevel, blockpos1, Direction.UP)) {
                        pLevel.setBlockAndUpdate(blockpos1.above(), net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(pLevel, blockpos1.above(), pPos, Blocks.FIRE.defaultBlockState()));
                    }
                }
            }

        }
    }

    private boolean hasFlammableNeighbours(LevelReader pLevel, BlockPos pPos) {
        for(Direction direction : Direction.values()) {
            if (this.isFlammable(pLevel, pPos.relative(direction), direction.getOpposite())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean supportsBoating(FluidState state, Boat boat) {
        return false;
    }

    private boolean isFlammable(LevelReader level, BlockPos pos, Direction face) {
        return (pos.getY() < level.getMinBuildHeight() ||
                pos.getY() >= level.getMaxBuildHeight() ||
                level.hasChunkAt(pos)) && level.getBlockState(pos).isFlammable(level, pos, face);
    }

    @Override
    public boolean canConvertToSource(Level level) {
        return level.getGameRules().getBoolean(GameRules.RULE_LAVA_SOURCE_CONVERSION);
    }

    // TODO Add particle
    @Override
    @Nullable
    public ParticleOptions getDripParticle() {
        return super.getDripParticle();
    }

    public static class Flowing extends Pahoehoe {
        protected Flowing(Properties properties) {
            super(properties);
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> pBuilder) {
            super.createFluidStateDefinition(pBuilder);
            pBuilder.add(LEVEL);
        }

        @Override
        public boolean isSource(@NotNull FluidState pState) {
            return false;
        }

        @Override
        public int getAmount(@NotNull FluidState pState) {
            return pState.getValue(LEVEL);
        }
    }

    public static class Source extends Pahoehoe {

        protected Source(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isSource(@NotNull FluidState pState) {
            return true;
        }

        @Override
        public int getAmount(@NotNull FluidState pState) {
            return 8;
        }
    }
}
