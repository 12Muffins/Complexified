package net.muffin.complexified.block.visual;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.muffin.complexified.block.custom.BoilerHeaterBlock;
import net.muffin.complexified.block.entity.BoilerHeaterBlockEntity;
import net.muffin.complexified.block.renderer.ComplexifiedPartialModels;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;

public class BoilerHeaterVisual extends AbstractBlockEntityVisual<BoilerHeaterBlockEntity> {

    protected OrientedInstance[] grates;

    public BoilerHeaterVisual(VisualizationContext context, BoilerHeaterBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        setupInstance();
    }

    private void setupInstance() {
        ArrayList<OrientedInstance> grateList = new ArrayList<>();
        for (Pair<Direction, BoilerHeaterBlock.GrateCorner.Edge> p: blockState.getValue(BoilerHeaterBlock.HEATER_GRATE).getGrateData()){

            Direction d = p.getFirst();
            BoilerHeaterBlock.GrateCorner.Edge e = p.getSecond();

            float yRot = -d.toYRot() + 180;
            OrientedInstance grate = instancerProvider().instancer(InstanceTypes.ORIENTED,
                    Models.partial(ComplexifiedPartialModels.GRATE_HEATER.get(e))).createInstance();
            grate = grate.rotateYDegrees(   yRot);
            transform(grate);
            grate.setChanged();

            grateList.add(grate);
        }

        grates = grateList.toArray(new OrientedInstance[0]);
    }

    protected OrientedInstance transform(OrientedInstance modelData) {
        BlockPos visualPosition = getVisualPosition();
        return modelData.translatePosition(visualPosition.getX(), visualPosition.getY(), visualPosition.getZ());
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        for (OrientedInstance grate: grates)
            consumer.accept(grate);
    }

    @Override
    public void updateLight(float partialTick) {
        relight(grates);
    }

    @Override
    protected void _delete() {
        for (OrientedInstance grate: grates)
            grate.delete();
    }

}
