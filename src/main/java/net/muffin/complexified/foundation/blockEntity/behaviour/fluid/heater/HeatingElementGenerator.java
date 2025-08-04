package net.muffin.complexified.foundation.blockEntity.behaviour.fluid.heater;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;
import net.muffin.complexified.block.custom.HeatingElementBlock;

public class HeatingElementGenerator extends SpecialBlockStateGen {

    private String material;

    public HeatingElementGenerator(String material) {
        this.material = material;
    }

    @Override
    protected int getXRotation(BlockState state) {
        return 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        return 0;
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                BlockState state) {
        String type = state.getValue(HeatingElementBlock.TYPE)
                .getSerializedName();

        String path = "block/boiler_heater/heating_element/heating_element_" + type;

        return prov.models()
                .withExistingParent(path + "_" + material, prov.modLoc(path))
                .texture("plate", prov.modLoc("block/boiler_heater/heating_element/" + material + "_plate"))
                .texture("fin", prov.modLoc("block/boiler_heater/heating_element/" + material + "_fin"));
    }

    public static <I extends BlockItem, P> NonNullFunction<ItemBuilder<I, P>, P> itemModel(String material) {
        return b -> b.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/boiler_heater/heating_element/heating_element_all"))
                        .texture("plate", p.modLoc("block/boiler_heater/heating_element/" + material + "_plate"))
                        .texture("fin", p.modLoc("block/boiler_heater/heating_element/" + material + "_fin")))
                .build();
    }

}
