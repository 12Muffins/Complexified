package net.muffin.complexified.foundation.blockEntity.behaviour.fluid.heater;

import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.muffin.complexified.block.custom.BoilerHeaterBlock;
import net.muffin.complexified.block.custom.HeatingElementBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public class HeatingElementBlockEntityBehaviour extends BlockEntityBehaviour {

    public static final BehaviourType<HeatingElementBlockEntityBehaviour> TYPE = new BehaviourType<>();

    private BlockState element;
    private boolean reRender;
    public Predicate<BlockState> updateElement;

    public HeatingElementBlockEntityBehaviour(SmartBlockEntity be, Predicate<BlockState> updateElement) {
        super(be);
        this.updateElement = updateElement;
    }

    public HeatingElementBlockEntityBehaviour(SmartBlockEntity be) {
        super(be);
        this.updateElement = (state) -> false;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public void applyElement(BlockState state) {
        this.element = state;
        reRender = true;
        blockEntity.notifyUpdate();
        Level world = getWorld();
        if (world.isClientSide)
            return;
        blockEntity.getBlockState()
                .updateNeighbourShapes(world, getPos(), 3);
    }

    public void transformElement(StructureTransform transform) {
        if (isElementPresent()) {
            BlockState transformedElement = transform.apply(element);
            applyElement(transformedElement);
        }
    }

    @Nullable
    public BlockState removeElement(boolean inOnReplacedContext) {
        if (element == null) {
            return null;
        }

        BlockState removed = this.element;
        Level world = getWorld();
        if (!world.isClientSide)
            world.levelEvent(2001, getPos(), Block.getId(element));
        this.element = null;
        reRender = true;
        if (inOnReplacedContext) {
            blockEntity.sendData();
            return removed;
        }
        blockEntity.notifyUpdate();
        if (world.isClientSide)
            return removed;
        blockEntity.getBlockState()
                .updateNeighbourShapes(world, getPos(), 3);
        return removed;
    }

    public boolean isElementPresent() {
        return element != null;
    }

    public boolean isElementValid(@NotNull BlockState elementState) {
        return elementState.getBlock() instanceof HeatingElementBlock;
    }

    @Nullable
    public BlockState getElement() {
        return element;
    }

    @Override
    public ItemRequirement getRequiredItems() {
        if (!isElementPresent()) {
            return ItemRequirement.NONE;
        }
        return ItemRequirement.of(element, null);
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        if (isElementPresent() && isElementValid(element)) {
            nbt.put("Element", NbtUtils.writeBlockState(element));
        }
        if (clientPacket && reRender) {
            NBTHelper.putMarker(nbt, "Redraw");
            reRender = false;
        }
        super.write(nbt, clientPacket);
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        if (nbt.contains("Element")) {
            element = null;
            BlockState readBlockState = NbtUtils.readBlockState(blockEntity.blockHolderGetter(), nbt.getCompound("Element"));
            if (isElementValid(readBlockState))
                element = readBlockState;
        }
        if (clientPacket && nbt.contains("Redraw"))
            getWorld().sendBlockUpdated(getPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 16);
        super.read(nbt, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();

//        BlockState entityState = blockEntity.getBlockState();
//
//        if (element != null && updateElement.test(entityState)) {
//            Optional<BlockState> suitableElement = ((HeatingElementBlock) element.getBlock()).getSuitableElement(entityState, Direction.UP);
//
//            suitableElement.ifPresent(blockState -> element = blockState);
//            blockEntity.sendData();
//            blockEntity.setChanged();
//        }
    }

    public void updateHeatingElement(BoilerHeaterBlock.GrateCorner form) {
        if (element != null && element.getValue(HeatingElementBlock.TYPE) != form) {
            blockEntity.getBehaviour(TYPE).applyElement(element.getBlock().defaultBlockState().setValue(HeatingElementBlock.TYPE, form));
        }
    }
}

