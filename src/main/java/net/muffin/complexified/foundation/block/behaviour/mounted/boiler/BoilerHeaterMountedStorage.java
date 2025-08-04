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
import net.muffin.complexified.foundation.blockEntity.behaviour.fluid.heater.HeatingElementBlockEntityBehaviour;
import org.jetbrains.annotations.Nullable;

public class BoilerHeaterMountedStorage extends WrapperMountedFluidStorage<FluidTankMountedStorage.Handler> {
    public static final Codec<BoilerHeaterMountedStorage> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(BoilerHeaterMountedStorage::getCapacity),
            FluidStack.CODEC.fieldOf("fluid").forGetter(BoilerHeaterMountedStorage::getFluid),
            BlockState.CODEC.fieldOf("heating_element").forGetter(BoilerHeaterMountedStorage::getHeatingElement)
    ).apply(i, BoilerHeaterMountedStorage::new));

    protected BlockState element;

    public BoilerHeaterMountedStorage(int capacity, FluidStack stack, BlockState element) {
        this(capacity, stack);
        this.element = element;
    }


    private Integer getCapacity() {
        return this.wrapped.getCapacity();
    }

    public FluidStack getFluid() {
        return this.wrapped.getFluid();
    }

    protected BlockState getHeatingElement() {
        return this.element;
    }

    protected void setHeatingElement(BoilerHeaterBlockEntity be,BlockState element) {
        be.getBehaviour(HeatingElementBlockEntityBehaviour.TYPE).applyElement(this.element);
    }

    protected BoilerHeaterMountedStorage(int capacity, FluidStack stack) {
        this(ComplexifiedMountedStorageTypes.BOILER_HEATER.get(), capacity, stack);
    }

    protected BoilerHeaterMountedStorage(MountedFluidStorageType<?> type, int capacity, FluidStack stack) {
        super(type, new FluidTankMountedStorage.Handler(capacity, stack));
    }

    public static BoilerHeaterMountedStorage fromEntity(BoilerHeaterBlockEntity heater) {
        FluidTank inventory;
        if (heater.isController()) {
        // heater has update callbacks, make an isolated copy
            inventory = heater.getTankInventory();
        } else {
            inventory = new FluidTank(0,(f) -> false );
        }

        BlockState element = heater.getBehaviour(HeatingElementBlockEntityBehaviour.TYPE).getElement();
        return new BoilerHeaterMountedStorage(inventory.getCapacity(), inventory.getFluid().copy(), element);

    }

    @Override
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof BoilerHeaterBlockEntity heater) {
            if (heater.isController()) {
                FluidTank inventory = heater.getTankInventory();
                inventory.setFluid(this.wrapped.getFluid());
            }
            setHeatingElement(heater, element);
        }
    }
}
