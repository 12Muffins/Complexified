package net.muffin.complexified.foundation.block.behaviour.mounted.boiler;

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.muffin.complexified.block.entity.BoilerHeaterBlockEntity;
import org.jetbrains.annotations.Nullable;

public class BoilerHeaterMountedStorageType extends MountedFluidStorageType<BoilerHeaterMountedStorage> {
    public BoilerHeaterMountedStorageType() {
        super(BoilerHeaterMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public BoilerHeaterMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof BoilerHeaterBlockEntity heater) {
            return BoilerHeaterMountedStorage.fromEntity(heater);
        }

        return null;
    }
}
