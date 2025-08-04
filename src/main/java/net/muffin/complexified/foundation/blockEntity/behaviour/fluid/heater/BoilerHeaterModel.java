package net.muffin.complexified.foundation.blockEntity.behaviour.fluid.heater;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.muffin.complexified.ComplexifiedSpriteShifts;
import net.muffin.complexified.block.custom.BoilerHeaterBlock;
import net.muffin.complexified.block.custom.HeatingElementBlock;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BoilerHeaterModel extends CTModel {

    protected static final ModelProperty<BoilerHeaterModel.CullData> CULL_PROPERTY = new ModelProperty<>();
    protected static final ModelProperty<BoilerHeaterModel.HeatingElementModelData> ELEMENT_PROPERTY = new ModelProperty<>();


    public BoilerHeaterModel(BakedModel originalModel) {
        super(originalModel, new BoilerHeaterCTBehaviour(
                ComplexifiedSpriteShifts.BOILER_HEATER_SIDE,
                ComplexifiedSpriteShifts.BOILER_HEATER_TOP_FRAME,
                ComplexifiedSpriteShifts.BOILER_HEATER_GRATE_TOP,
                ComplexifiedSpriteShifts.BOILER_HEATER_INNER_TANK,
                ComplexifiedSpriteShifts.BOILER_HEATER_INNER_TANK_TOP,
                ComplexifiedSpriteShifts.BOILER_HEATER_UNDERSIDE));
    }

    private void addQuads(List<BakedQuad> quads, BlockState state, Direction side, RandomSource rand, ModelData data,
                          HeatingElementModelData heaterData, RenderType renderType) {
        BakedModel heatingElement = heaterData.getElement();
        if (heatingElement != null)
            quads.addAll(heatingElement.getQuads(state, side, rand, data, renderType));
    }

    public static class HeatingElementModelData {
        private BakedModel element;

        public BakedModel getElement() {
            return element;
        }

        public void putElement(BlockState state) {
            if (state != null) {
                this.element = Minecraft.getInstance()
                        .getBlockRenderer()
                        .getBlockModel(state);
            }
        }
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
        if (side != null)
            return Collections.emptyList();

        List<BakedQuad> quads = new ArrayList<>();
        for (Direction d : Iterate.directions) {
            if (extraData.has(CULL_PROPERTY) && extraData.get(CULL_PROPERTY)
                    .isCulled(d))
                continue;
            quads.addAll(super.getQuads(state, d, rand, extraData, renderType));
        }

        if (extraData.has(ELEMENT_PROPERTY)) {
            HeatingElementModelData heaterData = extraData.get(ELEMENT_PROPERTY);
            quads = new ArrayList<>(quads);
            if (heaterData != null) {
                addQuads(quads, state, side, rand, extraData, heaterData, renderType);
            }
        }

        quads.addAll(super.getQuads(state, null, rand, extraData, renderType));
        return quads;
    }

    @Override
    protected ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state,
                                                ModelData blockEntityData) {
        super.gatherModelData(builder, world, pos, state, blockEntityData);
        HeatingElementModelData elementData = new HeatingElementModelData();
        BoilerHeaterModel.CullData cullData = new BoilerHeaterModel.CullData();
        HeatingElementBlockEntityBehaviour element = BlockEntityBehaviour.get(world, pos, HeatingElementBlockEntityBehaviour.TYPE);


        if (element != null) {
            elementData.putElement(element.getElement());
        }

        for (Direction d : Iterate.horizontalDirections)
            cullData.setCulled(d, ConnectivityHandler.isConnected(world, pos, pos.relative(d)));
        return builder.with(CULL_PROPERTY, cullData).with(ELEMENT_PROPERTY, elementData);
    }

    private static class CullData {
        boolean[] culledFaces;

        public CullData() {
            culledFaces = new boolean[4];
            Arrays.fill(culledFaces, false);
        }

        void setCulled(Direction face, boolean cull) {
            if (face.getAxis()
                    .isVertical())
                return;
            culledFaces[face.get2DDataValue()] = cull;
        }

        boolean isCulled(Direction face) {
            if (face.getAxis()
                    .isVertical())
                return false;
            return culledFaces[face.get2DDataValue()];
        }
    }
}
