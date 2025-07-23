package net.muffin.complexified.api.dataGen.recipe;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import net.minecraft.data.PackOutput;
import net.muffin.complexified.recipe.ModRecipeTypes;

public abstract class BoilerHeatingRecipeGen extends ProcessingRecipeGen {
    public BoilerHeatingRecipeGen(PackOutput generator, String defaultNamespace) {
        super(generator, defaultNamespace);
    }

    @Override
    protected ModRecipeTypes getRecipeType() {
        return ModRecipeTypes.BOILER_HEATING;
    }
}
