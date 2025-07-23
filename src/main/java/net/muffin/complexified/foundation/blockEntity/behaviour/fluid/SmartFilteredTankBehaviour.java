package net.muffin.complexified.foundation.blockEntity.behaviour.fluid;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.slf4j.Logger;

import java.util.Arrays;
public class SmartFilteredTankBehaviour extends SmartFluidTankBehaviour {
    public static final Logger LOGGER = LogUtils.getLogger();

    protected Fluid[] filteredFluids = new Fluid[0];
    public SmartFilteredTankBehaviour(BehaviourType<SmartFluidTankBehaviour> type, SmartBlockEntity be, int tanks, int tankCapacity, boolean enforceVariety) {
        super(type, be, tanks, tankCapacity, enforceVariety);

        IFluidHandler[] handlers = new IFluidHandler[tanks];
        for (int i = 0; i < tanks; i++) {
            TankSegment tankSegment = new TankSegment(tankCapacity);
            this.tanks[i] = tankSegment;
            handlers[i] = tankSegment.getTankSegment();
        }
        capability = LazyOptional.of(() -> new InternalFluidHandler(handlers, enforceVariety));
    }

    public SmartFilteredTankBehaviour(BehaviourType<SmartFluidTankBehaviour> type, SmartBlockEntity be, int tanks, int tankCapacity, boolean enforceVariety, Fluid filter) {
        this(type, be, tanks, tankCapacity, enforceVariety);

        filteredFluids = new Fluid[]{filter};
    }

    public SmartFilteredTankBehaviour(BehaviourType<SmartFluidTankBehaviour> type, SmartBlockEntity be, int tanks, int tankCapacity, boolean enforceVariety, Fluid[] filters) {
        this(type, be, tanks, tankCapacity, enforceVariety);
        filteredFluids = filters;
    }

        public class InternalFluidHandler extends SmartFluidTankBehaviour.InternalFluidHandler {
        public InternalFluidHandler(IFluidHandler[] handlers, boolean enforceVariety) {
            super(handlers, enforceVariety);
            LOGGER.info("Created Handler");
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
//            LOGGER.info("called Override fill");

            Fluid resourceFluid = resource.getFluid();
            boolean found = Arrays.stream(filteredFluids).anyMatch(fluid -> fluid == resourceFluid);

            if (!found && filteredFluids.length != 0) return 0;
            return super.fill(resource, action);
        }

        @Override
        public int forceFill(FluidStack resource, FluidAction action) {
//            LOGGER.info("called Override forceFill");
            Fluid resourceFluid = resource.getFluid();

            boolean found = Arrays.stream(filteredFluids).anyMatch(fluid -> fluid == resourceFluid);
            if (!found && filteredFluids.length != 0) return 0;

            return super.forceFill(resource, action);
        }
    }

    public class TankSegment extends SmartFluidTankBehaviour.TankSegment {
        public TankSegment(int capacity) {
            super(capacity);
        }

        public SmartFluidTank getTankSegment() { return this.tank; }
    }
}
