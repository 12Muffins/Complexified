package net.muffin.complexified.foundation.blockEntity.behaviour.fluid.heater;

import com.simibubi.create.foundation.data.AssetLookup;
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
        BoilerHeaterBlock.HeaterElement elementState = state.getValue(BoilerHeaterBlock.HEATING_ELEMENT);


//        if (elementState != BoilerHeaterBlock.HeaterElement.none) {
//            String suffix_element = state.getValue(BoilerHeaterBlock.HEATING_ELEMENT).getSerializedName();
//            String modelName = "boiler_heater" + "_element_" + suffix_element/* + '_' + suffix_grate*/;
//            return prov.models()
//                    .withExistingParent(modelName, prov.modLoc("block/boiler_heater/block_heating_element"))
//                    .texture("7", prov.modLoc("block/boiler_heater/heating_element/" + suffix_element + "_plate"))
//                    .texture("8", prov.modLoc("block/boiler_heater/heating_element/" + suffix_element + "_fin"));
//        }
        return AssetLookup.partialBaseModel(ctx, prov);
    }
}
