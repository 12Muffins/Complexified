package net.muffin.complexified.foundation.block.behaviour.mounted.boiler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.fluid.WrapperMountedFluidStorage;
import com.simibubi.create.content.fluids.tank.storage.FluidTankMountedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.muffin.complexified.block.entity.BoilerHeaterBlockEntity;
import net.muffin.complexified.foundation.block.behaviour.mounted.ComplexifiedMountedStorageTypes;
import org.jetbrains.annotations.Nullable;

public class BoilerHeaterMountedStorage extends WrapperMountedFluidStorage<FluidTankMountedStorage.Handler> {
    public static final Codec<BoilerHeaterMountedStorage> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(BoilerHeaterMountedStorage::getCapacity),
            FluidStack.CODEC.fieldOf("fluid").forGetter(BoilerHeaterMountedStorage::getFluid)
    ).apply(i, BoilerHeaterMountedStorage::new));

    private Integer getCapacity() {
        return this.wrapped.getCapacity();
    }

    public FluidStack getFluid() {
        return this.wrapped.getFluid();
    }

    protected BoilerHeaterMountedStorage(int capacity, FluidStack stack) {
        this(ComplexifiedMountedStorageTypes.BOILER_HEATER.get(), capacity, stack);
    }

    protected BoilerHeaterMountedStorage(MountedFluidStorageType<?> type, int capacity, FluidStack stack) {
        super(type, new FluidTankMountedStorage.Handler(capacity, stack));
    }

    public static BoilerHeaterMountedStorage fromTank(BoilerHeaterBlockEntity heater) {
        // heater has update callbacks, make an isolated copy
        FluidTank inventory = heater.getTankInventory();
        return new BoilerHeaterMountedStorage(inventory.getCapacity(), inventory.getFluid().copy());
    }

    @Override
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof BoilerHeaterBlockEntity tank && tank.isController()) {
            FluidTank inventory = tank.getTankInventory();
            // capacity shouldn't change, leave it
            inventory.setFluid(this.wrapped.getFluid());
        }
    }
}
