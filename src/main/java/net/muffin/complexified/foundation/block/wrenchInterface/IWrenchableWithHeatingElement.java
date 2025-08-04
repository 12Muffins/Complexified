package net.muffin.complexified.foundation.block.wrenchInterface;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.FluidPropagator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public interface IWrenchableWithHeatingElement extends IWrenchable {

    public Optional<ItemStack> removeElement(BlockGetter world, BlockPos pos, boolean inOnReplacedContext);

    default boolean tryRemoveElement(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Optional<ItemStack> element = removeElement(world, pos, false);
        if (element.isPresent()) {
            Player player = context.getPlayer();
            if (!world.isClientSide && !player.isCreative())
                player.getInventory().placeItemBackInInventory(element.get());
            return true;
        }
        return false;
    }

}
