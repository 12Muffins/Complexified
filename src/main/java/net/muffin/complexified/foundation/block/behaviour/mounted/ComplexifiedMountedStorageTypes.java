package net.muffin.complexified.foundation.block.behaviour.mounted;

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.muffin.complexified.Complexified;
import net.muffin.complexified.foundation.block.behaviour.mounted.boiler.BoilerHeaterMountedStorageType;

import java.util.function.Supplier;

public class ComplexifiedMountedStorageTypes {
    private static final CreateRegistrate REGISTRATE = Complexified.registrate();

    public static final RegistryEntry<BoilerHeaterMountedStorageType> BOILER_HEATER =
            simpleFluid("boiler_heater", BoilerHeaterMountedStorageType::new);


    private static <T extends MountedItemStorageType<?>> RegistryEntry<T> simpleItem(String name, Supplier<T> supplier) {
        return REGISTRATE.mountedItemStorage(name, supplier).register();
    }

    private static <T extends MountedFluidStorageType<?>> RegistryEntry<T> simpleFluid(String name, Supplier<T> supplier) {
        return REGISTRATE.mountedFluidStorage(name, supplier).register();
    }

    public static void register() {
    }
}
