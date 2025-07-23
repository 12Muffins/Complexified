package net.muffin.complexified.fluid;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.*;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.muffin.complexified.Complexified;
import net.muffin.complexified.item.ComplexifiedCreativeModeTabs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModFluids {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final CreateRegistrate REGISTRATE = Complexified.registrate();

    static {
        REGISTRATE.setCreativeTab(ComplexifiedCreativeModeTabs.ITEMS_CREATIVE_TAB);
    }

    public static final FluidEntry<ForgeFlowingFluid.Flowing> RESIN =
            REGISTRATE.standardFluid("spruce_resin")
//            REGISTRATE.standardFluid("spruce_resin", ModFluids.SolidRenderedPlaceableFluidType.create(0xEAAE2F,
//                            () -> 1f / 8f * AllConfigs.client().honeyTransparencyMultiplier.getF()))
                    .lang("Spruce Resin")
                    .properties(b -> b.viscosity(2000)
                            .density(1400))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .source(ForgeFlowingFluid.Source::new)
                    .renderType(RenderType::translucent)
                    .block()
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
                    .build()
                    .bucket() // Add ItemTag
                    .tag(AllTags.forgeItemTag("buckets/resin"))
                    .build()
                    .register();

    public static final FluidEntry<ForgeFlowingFluid.Flowing> PAHOEHOE =
            REGISTRATE.standardFluid("pahoehoe", SolidRenderedPlaceableFluidType.create(0x573431,
                            () -> 1f / 8f * AllConfigs.client().honeyTransparencyMultiplier.getF()))
                    .lang("Pahoehoe")
                    .properties(b -> b.viscosity(2000)
                            .density(1400))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .source(Pahoehoe.Source::new)
                    .block()
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW).lightLevel((s) -> 10))
                    .build()
                    .bucket() // Add ItemTag
                    .tag(AllTags.forgeItemTag("buckets/pahoehoe"))
                    .build()
                    .register();

    public static void register() {
    }


    private static class SolidRenderedPlaceableFluidType extends TintedFluidType {

        private Vector3f fogColor;
        private Supplier<Float> fogDistance;

        public static FluidBuilder.FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
            return (p, s, f) -> {
                SolidRenderedPlaceableFluidType fluidType = new SolidRenderedPlaceableFluidType(p, s, f);
                fluidType.fogColor = new Color(fogColor, false).asVectorF();
                fluidType.fogDistance = fogDistance;
                return fluidType;
            };
        }

        private SolidRenderedPlaceableFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
            super(properties, stillTexture, flowingTexture);
        }

        @Override
        protected int getTintColor(FluidStack stack) {
            return NO_TINT;
        }

        @Override
        public int getTintColor(FluidState state, BlockAndTintGetter world, BlockPos pos) {
            return 0x00ffffff;
        }

        @Override
        protected Vector3f getCustomFogColor() {
            return fogColor;
        }

        @Override
        protected float getFogDistanceModifier() {
            return fogDistance.get();
        }

    }

    public static abstract class TintedFluidType extends FluidType {

        protected static final int NO_TINT = 0xffffffff;
        private ResourceLocation stillTexture;
        private ResourceLocation flowingTexture;

        public TintedFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
            super(properties);
            this.stillTexture = stillTexture;
            this.flowingTexture = flowingTexture;
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {

                @Override
                public ResourceLocation getStillTexture() {
                    return stillTexture;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return flowingTexture;
                }

                @Override
                public int getTintColor(FluidStack stack) {
                    return ModFluids.TintedFluidType.this.getTintColor(stack);
                }

                @Override
                public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                    return ModFluids.TintedFluidType.this.getTintColor(state, getter, pos);
                }

                @Override
                public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level,
                                                        int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                    Vector3f customFogColor = ModFluids.TintedFluidType.this.getCustomFogColor();
                    return customFogColor == null ? fluidFogColor : customFogColor;
                }

                @Override
                public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick,
                                            float nearDistance, float farDistance, FogShape shape) {
                    float modifier = ModFluids.TintedFluidType.this.getFogDistanceModifier();
                    float baseWaterFog = 96.0f;
                    if (modifier != 1f) {
                        RenderSystem.setShaderFogShape(FogShape.CYLINDER);
                        RenderSystem.setShaderFogStart(-8);
                        RenderSystem.setShaderFogEnd(baseWaterFog * modifier);
                    }
                }

            });
        }

        protected abstract int getTintColor(FluidStack stack);

        protected abstract int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos);

        protected Vector3f getCustomFogColor() {
            return null;
        }

        protected float getFogDistanceModifier() {
            return 1f;
        }
    }

    public abstract class Pahoehoe extends LavaFluid {

//        public Pahoehoe(Properties properties) {
//            super(properties);
//            LOGGER.info("Constructed Pahoehoe");
//        }

        public static class Flowing extends ForgeFlowingFluid.Flowing {
            public Flowing(Properties properties) {
                super(properties);
            }

            protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> pBuilder) {
                super.createFluidStateDefinition(pBuilder);
                pBuilder.add(LEVEL);
            }

            public int getAmount(FluidState pState) {
                return pState.getValue(LEVEL);
            }

            public boolean isSource(FluidState pState) {
                return false;
            }
        }

        public static class Source extends ForgeFlowingFluid.Source {
            public Source(Properties properties) {
                super(properties);
            }

            public int getAmount(FluidState pState) {
                return 8;
            }

            public boolean isSource(FluidState pState) {
                return true;
            }
        }
//        // TODO Add particle
//        @Override
//        protected @Nullable ParticleOptions getDripParticle() {
//            LOGGER.info("Called getDripParticle()");
//            return super.getDripParticle();
//        }
//
//        @Override
//        protected boolean isRandomlyTicking() {
//            LOGGER.info("Called isRandomlyTicking()");
//            return true;
//        }
//
//        @Override
//        public void animateTick(Level pLevel, BlockPos pPos, FluidState pState, RandomSource pRandom) {
//            LOGGER.info("Called animateTick()");
//
//            BlockPos blockpos = pPos.above();
//            if (pLevel.getBlockState(blockpos).isAir() && !pLevel.getBlockState(blockpos).isSolidRender(pLevel, blockpos)) {
//                if (pRandom.nextInt(100) == 0) {
//                    double d0 = (double)pPos.getX() + pRandom.nextDouble();
//                    double d1 = (double)pPos.getY() + 1.0D;
//                    double d2 = (double)pPos.getZ() + pRandom.nextDouble();
//                    pLevel.addParticle(ParticleTypes.LAVA, d0, d1, d2, 0.0D, 0.0D, 0.0D);
//                    pLevel.playLocalSound(d0, d1, d2, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.2F + pRandom.nextFloat() * 0.2F, 0.9F + pRandom.nextFloat() * 0.15F, false);
//                }
//                if (pRandom.nextInt(200) == 0) {
//                    pLevel.playLocalSound(pPos.getX(), pPos.getY(), pPos.getZ(), SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS, 0.2F + pRandom.nextFloat() * 0.2F, 0.9F + pRandom.nextFloat() * 0.15F, false);
//                }
//            }
//        }
//
//        @Override
//        protected void randomTick(Level pLevel, BlockPos pPos, FluidState pState, RandomSource pRandom) {
//            LOGGER.info("Called randomTick()");
//
//            if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
//                int i = pRandom.nextInt(3);
//                if (i > 0) {
//                    BlockPos blockpos = pPos;
//
//                    for(int j = 0; j < i; ++j) {
//                        blockpos = blockpos.offset(pRandom.nextInt(3) - 1, 1, pRandom.nextInt(3) - 1);
//                        if (!pLevel.isLoaded(blockpos)) {
//                            return;
//                        }
//
//                        BlockState blockstate = pLevel.getBlockState(blockpos);
//                        if (blockstate.isAir()) {
//                            if (this.hasFlammableNeighbours(pLevel, blockpos)) {
//                                pLevel.setBlockAndUpdate(blockpos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(pLevel, blockpos, pPos, Blocks.FIRE.defaultBlockState()));
//                                return;
//                            }
//                        } else if (blockstate.blocksMotion()) {
//                            return;
//                        }
//                    }
//                } else {
//                    for(int k = 0; k < 3; ++k) {
//                        BlockPos blockpos1 = pPos.offset(pRandom.nextInt(3) - 1, 0, pRandom.nextInt(3) - 1);
//                        if (!pLevel.isLoaded(blockpos1)) {
//                            return;
//                        }
//
//                        if (pLevel.isEmptyBlock(blockpos1.above()) && this.isFlammable(pLevel, blockpos1, Direction.UP)) {
//                            pLevel.setBlockAndUpdate(blockpos1.above(), net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(pLevel, blockpos1.above(), pPos, Blocks.FIRE.defaultBlockState()));
//                        }
//                    }
//                }
//
//            }
//        }
//
//        private boolean hasFlammableNeighbours(LevelReader pLevel, BlockPos pPos) {
//            for(Direction direction : Direction.values()) {
//                if (this.isFlammable(pLevel, pPos.relative(direction), direction.getOpposite())) {
//                    return true;
//                }
//            }
//
//            return false;
//        }
//        private boolean isFlammable(LevelReader level, BlockPos pos, Direction face) {
//            return (pos.getY() < level.getMinBuildHeight() ||
//                    pos.getY() >= level.getMaxBuildHeight() ||
//                    level.hasChunkAt(pos)) && level.getBlockState(pos).isFlammable(level, pos, face);
//        }
//
//        @Override
//        protected boolean canConvertToSource(Level level) {
//            LOGGER.info("Called canConvertToSource()");
//            return level.getGameRules().getBoolean(GameRules.RULE_LAVA_SOURCE_CONVERSION);
//        }
//    }
    }
}
