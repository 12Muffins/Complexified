package net.muffin.complexified.item;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.muffin.complexified.block.custom.HeatingElementBlock;
import net.muffin.complexified.foundation.blockEntity.behaviour.fluid.heater.HeatingElementBlockEntityBehaviour;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class HeatingElementBlockItem extends BlockItem {
    public HeatingElementBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        HeatingElementBlock heatingElementBlock = getHeatingElementBlock();
        Player player = context.getPlayer();

        HeatingElementBlockEntityBehaviour behaviour = BlockEntityBehaviour.get(world, pos, HeatingElementBlockEntityBehaviour.TYPE);

        if (behaviour == null)
            return InteractionResult.FAIL;
        if (world.isClientSide)
            return InteractionResult.SUCCESS;

        Optional<BlockState> heatingElement = heatingElementBlock.getSuitableElement(state, context.getClickedFace());

        if (heatingElement.isEmpty())
            return InteractionResult.SUCCESS;

        BlockState bracket = behaviour.getElement();
        BlockState newElement = heatingElement.get();

        if (bracket == newElement)
            return InteractionResult.SUCCESS;

        world.playSound(null, pos, newElement
                .getSoundType()
                .getPlaceSound(), SoundSource.BLOCKS, 0.75f, 1);
        behaviour.applyElement(newElement);

        if (player == null || !player.isCreative()) {
            context.getItemInHand()
                    .shrink(1);
            if (bracket != null) {
                ItemStack returnedStack = new ItemStack(bracket.getBlock());
                if (player == null)
                    Block.popResource(world, pos, returnedStack);
                else
                    player.getInventory().placeItemBackInInventory(returnedStack);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private HeatingElementBlock getHeatingElementBlock() {
        return (HeatingElementBlock) getBlock();
    }
}
