package net.muffin.complexified.foundation.blockEntity.behaviour.fluid.heater;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;
import net.muffin.complexified.block.custom.BoilerHeaterBlock;

public class BoilerHeaterGenerator extends SpecialBlockStateGen {

    @Override
    protected int getXRotation(BlockState state) {
        return 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        return 0;
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, BlockState state) {
        String suffix = state.getValue(BoilerHeaterBlock.HEATER_GRATE).getSerializedName();

        String modelName = "boiler_heater" + "_grate_" + suffix;
        return prov.models()
                .withExistingParent(modelName, prov.modLoc("block/boiler_heater/block"));
//                .texture("5", prov.modLoc("block/boiler_heater/grade_side"));
    }
}
