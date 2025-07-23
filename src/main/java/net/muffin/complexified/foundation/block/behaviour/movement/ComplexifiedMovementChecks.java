package net.muffin.complexified.foundation.block.behaviour.movement;

import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.impl.contraption.BlockMovementChecksImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.muffin.complexified.Complexified;
import net.muffin.complexified.block.custom.BoilerHeaterBlock;

public class ComplexifiedMovementChecks {
    static public void register() {
        BlockMovementChecks.registerAttachedCheck(ComplexifiedAttachedCheck.BOILER_HEATER);
    }

    public static class ComplexifiedAttachedCheck implements BlockMovementChecks.AttachedCheck {
        static BlockMovementChecks.AttachedCheck BOILER_HEATER = BoilerHeaterBlock::isBlockAttachedTowards;

        @Override
        public BlockMovementChecks.CheckResult isBlockAttachedTowards(BlockState state, Level world, BlockPos pos, Direction direction) {
            return BlockMovementChecks.CheckResult.of(BlockMovementChecksImpl.isBlockAttachedTowards(state, world, pos, direction));
        }
    }
}
