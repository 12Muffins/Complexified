package net.muffin.complexified.block.renderer;

import com.simibubi.create.AllPartialModels;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.muffin.complexified.Complexified;
import net.muffin.complexified.block.custom.BoilerHeaterBlock;

import java.util.EnumMap;
import java.util.Map;

public class ComplexifiedPartialModels extends AllPartialModels {
    public static final PartialModel
//            HEATER_GRATE = block("boiler_heater/grate/grate"),
            HEATER_GAUGE = block("boiler_heater/gauge"),
            HEATER_GAUGE_DIAL = block("boiler_heater/gauge_dial");

    public static final Map<BoilerHeaterBlock.GrateCorner.Edge, PartialModel>
            GRATE_HEATER = new EnumMap<>(BoilerHeaterBlock.GrateCorner.Edge.class);

    static {
        for (BoilerHeaterBlock.GrateCorner.Edge grate: BoilerHeaterBlock.GrateCorner.Edge.values()) {
            GRATE_HEATER.put(grate, block("boiler_heater/grate/grate_" + grate.getSerializedName()));
        }
    }

    private static PartialModel block(String path) {
        return PartialModel.of(Complexified.asResource("block/" + path));
    }

    public static void init() {
        // init static fields
    }

}
