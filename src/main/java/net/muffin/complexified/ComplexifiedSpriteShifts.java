package net.muffin.complexified;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTType;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SpriteShifter;

public class ComplexifiedSpriteShifts {

    public static final CTSpriteShiftEntry BOILER_HEATER_GRATE_TOP = getCT(AllCTTypes.RECTANGLE,"boiler_heater/grate_top"),
            BOILER_HEATER_GRADE_SIDE = getCT(AllCTTypes.RECTANGLE, "boiler_heater/grade_side"),
            BOILER_HEATER_TOP_FRAME = getCT(AllCTTypes.RECTANGLE, "boiler_heater/grate_top_frame"),
            BOILER_HEATER_SIDE = getCT(AllCTTypes.RECTANGLE, "boiler_heater/heater_side"),
            BOILER_HEATER_INNER_TANK = getCT(AllCTTypes.RECTANGLE, "boiler_heater/inner_tank"),
            BOILER_HEATER_INNER_TANK_TOP = getCT(AllCTTypes.RECTANGLE, "boiler_heater/inner_tank_top"),
            BOILER_HEATER_UNDERSIDE = getCT(AllCTTypes.RECTANGLE, "boiler_heater/underside");


    private static CTSpriteShiftEntry omni(String name) {
        return getCT(AllCTTypes.OMNIDIRECTIONAL, name);
    }

    private static CTSpriteShiftEntry horizontal(String name) {
        return getCT(AllCTTypes.HORIZONTAL, name);
    }

    private static CTSpriteShiftEntry vertical(String name) {
        return getCT(AllCTTypes.VERTICAL, name);
    }

    //

    private static SpriteShiftEntry get(String originalLocation, String targetLocation) {
        return SpriteShifter.get(Complexified.asResource(originalLocation), Complexified.asResource(targetLocation));
    }

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName, String connectedTextureName) {
        return CTSpriteShifter.getCT(type, Complexified.asResource("block/" + blockTextureName),
                Complexified.asResource("block/" + connectedTextureName + "_connected"));
    }

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName) {
        return getCT(type, blockTextureName, blockTextureName);
    }

}
