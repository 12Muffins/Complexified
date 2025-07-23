package net.muffin.complexified.foundation.blockEntity.behaviour.fluid.heater;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BoilerHeaterCTBehaviour extends HorizontalCTBehaviour {
    private CTSpriteShiftEntry grateTop;
    private CTSpriteShiftEntry innerTank;
    private CTSpriteShiftEntry innerTankTop;
    private CTSpriteShiftEntry underside;

    public BoilerHeaterCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift, CTSpriteShiftEntry grateTop,
                                   CTSpriteShiftEntry innerTank, CTSpriteShiftEntry innerTankTop, CTSpriteShiftEntry underside) {
        super(layerShift, topShift);
        this.grateTop = grateTop;
        this.innerTank = innerTank;
        this.innerTankTop = innerTankTop;
        this.underside = underside;
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        if (sprite != null && direction.getAxis() == Direction.Axis.Y && grateTop.getOriginal() == sprite) return grateTop;
        if (sprite != null && direction.getAxis() == Direction.Axis.Y && underside.getOriginal() == sprite) return underside;
        if (sprite != null && direction.getAxis() == Direction.Axis.Y && innerTankTop.getOriginal() == sprite) return innerTankTop;
        if (sprite != null && direction.getAxis().isHorizontal() &&  innerTank.getOriginal() == sprite) {
            return innerTank;
        }
        return super.getShift(state, direction, sprite);

    }

    @Override
    public boolean buildContextForOccludedDirections() {
        return true;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos,
                              Direction face) {
        return state.getBlock() == other.getBlock() && ConnectivityHandler.isConnected(reader, pos, otherPos);
    }

}
