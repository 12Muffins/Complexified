package net.muffin.complexified.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.muffin.complexified.block.custom.BoilerHeaterBlock;
import net.muffin.complexified.block.entity.BoilerHeaterBlockEntity;

public class BoilerHeaterRenderer extends SafeBlockEntityRenderer<BoilerHeaterBlockEntity> {

    public BoilerHeaterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(BoilerHeaterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState blockState = be.getBlockState();
        VertexConsumer vb = buffer.getBuffer(RenderType.cutout());


        if (!VisualizationManager.supportsVisualization(be.getLevel())) {
            for (Pair<Direction, BoilerHeaterBlock.GrateCorner.Edge> p: blockState.getValue(BoilerHeaterBlock.HEATER_GRATE).getGrateData()) {

                Direction d = p.getFirst();
                BoilerHeaterBlock.GrateCorner.Edge e = p.getSecond();

                float yRot = -d.toYRot() + 180;
                CachedBuffers.partial(ComplexifiedPartialModels.GRATE_HEATER.get(e), blockState)
                        .translate(0.5,0.5,0.5)
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .light(light)
                        .renderInto(ms, vb);
            }
//            for (Direction d: blockState.getValue(BoilerHeaterBlock.HEATER_GRATE).getDirections()){
//                float yRot = -d.toYRot() + 180;
//                CachedBuffers.partial(ComplexifiedPartialModels.HEATER_GRATE, blockState)
//                        .translate(0.5,0.5,0.5)
//                        .rotateYDegrees(yRot)
//                        .uncenter()
//                        .light(light)
//                        .renderInto(ms, vb);
//            }
        }

        if (!be.isController()) return;
        if (be.getWidth() == 1) return;

        ms.pushPose();
        PoseTransformStack msr = TransformStack.of(ms);
        msr.translate(be.getWidth() / 2f, 0.5, be.getWidth() / 2f);

        float dialPivotY = 6f / 16;
        float dialPivotZ = 8f / 16;
        float progress = be.heating.gauge.getValue(partialTicks);

        for (Direction d : Iterate.horizontalDirections) {
            if (be.heating.occludedDirections[d.get2DDataValue()])
                continue;
            ms.pushPose();
            float yRot = -d.toYRot() - 90;
            CachedBuffers.partial(ComplexifiedPartialModels.HEATER_GAUGE, blockState)
                    .rotateYDegrees(yRot)
                    .uncenter()
                    .translate(be.getWidth() / 2f - 7.03 / 16f, 0, 0)
                    .light(light)
                    .renderInto(ms, vb);
            CachedBuffers.partial(ComplexifiedPartialModels.HEATER_GAUGE_DIAL, blockState)
                    .rotateYDegrees(yRot)
                    .uncenter()
                    .translate(be.getWidth() / 2f - 7.03 / 16f, 0, 0)
                    .translate(0, dialPivotY, dialPivotZ)
                    .rotateXDegrees(-145 * progress + 90)
                    .translate(0, -dialPivotY, -dialPivotZ)
                    .light(light)
                    .renderInto(ms, vb);
            ms.popPose();
        }

        ms.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(BoilerHeaterBlockEntity be) {
        return be.isController();
    }
}
