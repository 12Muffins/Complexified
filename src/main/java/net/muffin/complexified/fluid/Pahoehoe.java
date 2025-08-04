package net.muffin.complexified.fluid;

import com.tterrag.registrate.builders.FluidBuilder;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.IFluidBlock;
import net.muffin.complexified.block.ModBlocks;
import net.muffin.complexified.block.custom.AndesiteMachineBlock;
import net.muffin.complexified.tag.ModTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;

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

                for (int j = 0; j < i; ++j) {
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
                for (int k = 0; k < 3; ++k) {
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
        for (Direction direction : Direction.values()) {
            if (this.isFlammable(pLevel, pPos.relative(direction), direction.getOpposite())) {
                return true;
            }
        }

        return false;
    }

//    @Override
//    public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
//
//        double d0 = 0.08D;
//        boolean flag = entity.getDeltaMovement().y <= 0.0D;
//
//        double d8 = entity.getY();
//        entity.moveRelative(0.02F, movementVector);
//        entity.move(MoverType.SELF, entity.getDeltaMovement());
//        if (entity.getFluidHeight(FluidTags.LAVA) <= entity.getFluidJumpThreshold()) {
//            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5D, (double)0.8F, 0.5D));
//            Vec3 vec33 = entity.getFluidFallingAdjustedMovement(d0, flag, entity.getDeltaMovement());
//            entity.setDeltaMovement(vec33);
//        } else {
//            entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5D));
//        }
//
//        if (!entity.isNoGravity()) {
//            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, -d0 / 4.0D, 0.0D));
//        }
//
//        Vec3 vec34 = entity.getDeltaMovement();
//        if (entity.horizontalCollision && entity.isFree(vec34.x, vec34.y + (double)0.6F - entity.getY() + d8, vec34.z)) {
//            entity.setDeltaMovement(vec34.x, (double)0.3F, vec34.z);
//        }
//
//        // TODO: Implement it correctly please
//        entity.lavaHurt();
//
//        return true;
//    }

    @Override
    public boolean supportsBoating(FluidState state, Boat boat) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor worldIn, BlockPos pos, BlockState state) {
        fizz(worldIn, pos);
    }

    private void fizz(LevelAccessor pLevel, BlockPos pPos) {
        pLevel.levelEvent(1501, pPos, 0);
    }


    private boolean isFlammable(LevelReader level, BlockPos pos, Direction face) {
        return (pos.getY() < level.getMinBuildHeight() ||
                pos.getY() >= level.getMaxBuildHeight() ||
                level.hasChunkAt(pos)) && level.getBlockState(pos).isFlammable(level, pos, face);
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

    public static class FluidType extends ModFluids.SolidRenderedPlaceableFluidType {

        private FluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
            super(properties, stillTexture, flowingTexture);
        }

        public static FluidBuilder.FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
            return (p, s, f) -> {
                ModFluids.SolidRenderedPlaceableFluidType fluidType = new FluidType(p, s, f);
                fluidType.fogColor = new Color(fogColor, false).asVectorF();
                fluidType.fogDistance = fogDistance;
                return fluidType;
            };
        }

        @Override
        public boolean canSwim(Entity entity) {
            return false;
        }

        @Override
        public boolean canConvertToSource(FluidState state, LevelReader reader, BlockPos pos) {
            return ((Level) reader).getGameRules().getBoolean(GameRules.RULE_LAVA_SOURCE_CONVERSION);
        }

        @Override
        public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
            return level.dimensionType().ultraWarm();
        }

//        @Override
//        public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
//            double d0 = 0.08D;
//            boolean flag = entity.getDeltaMovement().y <= 0.0D;
//
//            double d8 = entity.getY();
//            entity.moveRelative(0.02F, movementVector);
//            entity.move(MoverType.SELF, entity.getDeltaMovement());
//            if (entity.getFluidTypeHeight(ModFluids.PAHOEHOE.getType()) <= entity.getFluidJumpThreshold()) {
//                entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5D, (double)0.8F, 0.5D));
//                Vec3 vec33 = entity.getFluidFallingAdjustedMovement(d0, flag, entity.getDeltaMovement());
//                entity.setDeltaMovement(vec33);
//            } else {
//                entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5D));
//            }
//
//            if (!entity.isNoGravity()) {
//                entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, -d0 / 4.0D, 0.0D));
//            }
//
//            Vec3 vec34 = entity.getDeltaMovement();
//            if (entity.horizontalCollision && entity.isFree(vec34.x, vec34.y + (double)0.6F - entity.getY() + d8, vec34.z)) {
//                entity.setDeltaMovement(vec34.x, (double)0.3F, vec34.z);
//            }
////
//
////            TODO: Implement it correctly please
////            entity.lavaHurt();
//
//            return false;
//        }

        public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
            double d0 = 0.08D;
            if (entity.onGround() && entity.level().getFluidState(entity.getOnPos()).is(ModTags.FluidTags.PAHOEHOE.tag)) {
                BlockPos blockpos = getBlockPosBelowThatAffectsMyMovement(entity);
                float f2 = entity.level().getBlockState(getBlockPosBelowThatAffectsMyMovement(entity)).getFriction(entity.level(), getBlockPosBelowThatAffectsMyMovement(entity), entity);
                float f3 = entity.onGround() ? f2 * 0.91F : 0.91F;
                Vec3 vec35 = entity.handleRelativeFrictionAndCalculateMovement(movementVector, f2);
                double d2 = vec35.y;
                if (entity.hasEffect(MobEffects.LEVITATION)) {
                    d2 += (0.05D * (double)(entity.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vec35.y) * 0.2D;
                } else if (entity.level().isClientSide && !entity.level().hasChunkAt(blockpos)) {
                    if (entity.getY() > (double)entity.level().getMinBuildHeight()) {
                        d2 = -0.1D;
                    } else {
                        d2 = 0.0D;
                    }
                } else if (!entity.isNoGravity()) {
                    d2 -= d0;
                }

                if (entity.shouldDiscardFriction()) {
                    entity.setDeltaMovement(vec35.x, d2, vec35.z);
                } else {
                    entity.setDeltaMovement(vec35.x * (double)f3, d2 * (double)0.98F, vec35.z * (double)f3);
                }

            } else {
                boolean flag = entity.getDeltaMovement().y <= 0.0D;

                double d8 = entity.getY();
                entity.moveRelative(0.02F, movementVector);
                entity.move(MoverType.SELF, entity.getDeltaMovement());
                if (entity.getFluidHeight(FluidTags.LAVA) <= entity.getFluidJumpThreshold()) {
                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5D, (double) 0.8F, 0.5D));
                    Vec3 vec33 = entity.getFluidFallingAdjustedMovement(d0, flag, entity.getDeltaMovement());
                    entity.setDeltaMovement(vec33);
                } else {
                    entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5D));
                }

                if (!entity.isNoGravity()) {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, -d0 / 4.0D, 0.0D));
                }

                Vec3 vec34 = entity.getDeltaMovement();
                if (entity.horizontalCollision && entity.isFree(vec34.x, vec34.y + (double) 0.6F - entity.getY() + d8, vec34.z)) {
                    entity.setDeltaMovement(vec34.x, (double) 0.3F, vec34.z);
                }
            }

            return true;
        }



        public static BlockPos getBlockPosBelowThatAffectsMyMovement(LivingEntity entity) {
            return getOnPos(0.500001F, entity);
        }

        protected static BlockPos getOnPos(float pYOffset, LivingEntity entity) {
            if (entity.mainSupportingBlockPos.isPresent()) {
                BlockPos blockpos = entity.mainSupportingBlockPos.get();
                if (!(pYOffset > 1.0E-5F)) {
                    return blockpos;
                } else {
                    BlockState blockstate = entity.level().getBlockState(blockpos);
                    return (!((double)pYOffset <= 0.5D) || !blockstate.collisionExtendsVertically(entity.level(), blockpos, entity)) ? blockpos.atY(Mth.floor(entity.position().y - (double)pYOffset)) : blockpos;
                }
            } else {
                int i = Mth.floor(entity.position().x);
                int j = Mth.floor(entity.position().y - (double)pYOffset);
                int k = Mth.floor(entity.position().z);
                return new BlockPos(i, j, k);
            }
        }

        @Override
        public void setItemMovement(ItemEntity entity) {
            Vec3 vec3 = entity.getDeltaMovement();
            entity.setDeltaMovement(vec3.x * (double)0.95F, vec3.y + (double)(vec3.y < (double)0.06F ? 5.0E-4F : 0.0F), vec3.z * (double)0.95F);
        }

        @Override
        public void onVaporize(@Nullable Player player, Level level, BlockPos pos, FluidStack stack) {
            super.onVaporize(player, level, pos, stack);
            SoundEvent sound = this.getSound(player, level, pos, SoundActions.BUCKET_EMPTY);
            level.playSound(player, pos, sound != null ? sound : SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS);
            level.setBlock(pos, Fluids.LAVA.defaultFluidState().createLegacyBlock(), 2);
        }

        @Override
        public boolean supportsBoating(Boat boat) {
            return false;
        }
    }
    public static class LiquidBlock extends net.minecraft.world.level.block.LiquidBlock {
        public static final VoxelShape[] STABLE_SHAPE = makeShape();

        private static VoxelShape[] makeShape() {
            ArrayList<VoxelShape> stableShapes = new ArrayList<>();
            for (int i = 8; i > -8; i--) {
                stableShapes.add(Block.box(0,i*2-3,0,16,i*2-2,16));
            }
            stableShapes.set(8, Shapes.empty());

            return stableShapes.toArray(new VoxelShape[0]);
        }

        public LiquidBlock(Supplier<? extends FlowingFluid> pFluid, Properties pProperties) {
            super(pFluid, pProperties);
        }

        public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
            if (!pEntity.isSteppingCarefully() && pEntity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)pEntity)) {
                pEntity.hurt(pLevel.damageSources().hotFloor(), 1.0F);
            }

            super.stepOn(pLevel, pPos, pState, pEntity);
        }

        public @NotNull VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
            int value = pState.getValue(LEVEL);
            if (pLevel.getFluidState(pPos.above()).is(ModTags.FluidTags.PAHOEHOE.tag)) return Shapes.empty();
            return pContext.isAbove((STABLE_SHAPE)[(value+2)%16], pPos, true) ? (STABLE_SHAPE)[value%16] : Shapes.empty();
        }
    }
}
