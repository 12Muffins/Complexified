package net.muffin.complexified.datagen.recipe;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.material.Fluids;
import net.muffin.complexified.Complexified;
import net.muffin.complexified.api.dataGen.recipe.BoilerHeatingRecipeGen;
import net.muffin.complexified.fluid.ModFluids;

public final class ComplexifiedBoilerHeatingRecipeGen extends BoilerHeatingRecipeGen {

    GeneratedRecipe

    PAHOEHOE = create("pahoehoe_heated", b ->
            b.require(Fluids.LAVA, 1).output(ModFluids.PAHOEHOE.get(), 1).duration(0));

//    PAHOEHOE_LITTLE = create("pahoehoe_little", b ->
//            b.require(Fluids.LAVA, 63).output(Fluids.WATER, 63).duration(0));

    public ComplexifiedBoilerHeatingRecipeGen(PackOutput output) {
        super(output, Complexified.MOD_ID);
    }
}
