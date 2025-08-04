package net.muffin.complexified.fluid;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidInteractionRegistry.InteractionInformation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.muffin.complexified.Complexified;
import net.muffin.complexified.item.ComplexifiedCreativeModeTabs;
import net.muffin.complexified.tag.ModTags;
import org.jetbrains.annotations.NotNull;
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

    public static final FluidEntry<Pahoehoe.Flowing> PAHOEHOE =
            REGISTRATE.fluid("pahoehoe",
                            ResourceLocation.fromNamespaceAndPath(Complexified.MOD_ID, "fluid/pahoehoe_still"),
                            ResourceLocation.fromNamespaceAndPath(Complexified.MOD_ID, "fluid/pahoehoe_flow"),
                            Pahoehoe.FluidType.create(0x573431,
                                    () -> 1f / 32f),
                            Pahoehoe.Flowing::new
                    )
                    .lang("Pahoehoe")
                    .properties(b -> b
                            .viscosity(10000)
                            .fallDistanceModifier(1)
                            .density(1400)
                            .temperature(700)
                            .adjacentPathType(null)
                            .pathType(BlockPathTypes.LAVA)
                            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                            .lightLevel(8)
                    )
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .source(Pahoehoe.Source::new)
                    .block(Pahoehoe.LiquidBlock::new)
                    .tag(AllTags.AllBlockTags.FAN_PROCESSING_CATALYSTS_BLASTING.tag, ModTags.BlockTags.PAHOEHOE.tag)
//                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW).lightLevel((s) -> 10))
                    .build()
                    .bucket() // Add ItemTag
                    .tag(AllTags.forgeItemTag("buckets/pahoehoe"))
                    .build()
                    .register();

    public static void register() {
    }


    protected static class SolidRenderedPlaceableFluidType extends TintedFluidType {

        protected Vector3f fogColor;
        protected Supplier<Float> fogDistance;

        public static FluidBuilder.FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
            return (p, s, f) -> {
                SolidRenderedPlaceableFluidType fluidType = new SolidRenderedPlaceableFluidType(p, s, f);
                fluidType.fogColor = new Color(fogColor, false).asVectorF();
                fluidType.fogDistance = fogDistance;
                return fluidType;
            };
        }

        protected SolidRenderedPlaceableFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
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

    public static void registerFluidInteractions() {
        FluidInteractionRegistry.addInteraction(PAHOEHOE.getSource().getFluidType(), new InteractionInformation(
                AllFluids.HONEY.get().getFluidType(),
                fluidState -> fluidState.isSource() ? Blocks.OBSIDIAN.defaultBlockState() :
                        AllPaletteStoneTypes.LIMESTONE.getBaseBlock()
                            .get()
                            .defaultBlockState()
        ));

        FluidInteractionRegistry.addInteraction(PAHOEHOE.getSource().getFluidType(), new InteractionInformation(
                AllFluids.CHOCOLATE.get().getFluidType(),
                fluidState -> fluidState.isSource() ? Blocks.OBSIDIAN.defaultBlockState() :
                        AllPaletteStoneTypes.SCORIA.getBaseBlock()
                                .get()
                                .defaultBlockState()
        ));

        FluidInteractionRegistry.addInteraction(PAHOEHOE.get().getFluidType(), new InteractionInformation(
                ForgeMod.WATER_TYPE.get(),
                fluidState -> fluidState.isSource() ? Blocks.OBSIDIAN.defaultBlockState() :
                        Blocks.TUFF.defaultBlockState()
        ));
    }
}
