package net.muffin.complexified.block.custom;

import com.simibubi.create.content.decoration.bracket.BracketBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class HeatingElementBlock extends Block implements IWrenchable {

    public static final EnumProperty<BoilerHeaterBlock.GrateCorner> TYPE = EnumProperty.create("type", BoilerHeaterBlock.GrateCorner.class);

    public HeatingElementBlock(Properties pProperties) {
        super(pProperties);
    }

    public Optional<BlockState> getSuitableElement(BlockState state, Direction clickedFace) {
        if (!(state.getBlock() instanceof BoilerHeaterBlock) || clickedFace != Direction.UP) return Optional.empty();
        return Optional.of(defaultBlockState().setValue(TYPE, state.getValue(BoilerHeaterBlock.HEATER_GRATE)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(TYPE);
    }

    @Override
    public @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        BoilerHeaterBlock.GrateCorner type = state.getValue(TYPE);
        return state.setValue(TYPE, type.mirror(mirror));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        BoilerHeaterBlock.GrateCorner type = state.getValue(TYPE);
        type = type.rotate(rotation);
        return state.setValue(TYPE, type);
    }
}
