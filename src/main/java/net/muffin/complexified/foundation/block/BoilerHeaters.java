package net.muffin.complexified.foundation.block;

import com.simibubi.create.api.boiler.BoilerHeater;
import net.muffin.complexified.block.ModBlocks;
import net.muffin.complexified.block.custom.BoilerHeaterBlock;
import net.muffin.complexified.block.custom.BoilerHeaterBlockOld;

public class BoilerHeaters {
    public static void registerDefaults() {
        BoilerHeater.REGISTRY.register(ModBlocks.BOILER_HEATER_OLD.get(), BoilerHeaterBlockOld::getHeat);
        BoilerHeater.REGISTRY.register(ModBlocks.BOILER_HEATER_STRUCTURAL.get(), BoilerHeaterBlockOld::getHeat);
        BoilerHeater.REGISTRY.register(ModBlocks.BOILER_HEATER.get(), BoilerHeaterBlock::getHeat);
    }
}
