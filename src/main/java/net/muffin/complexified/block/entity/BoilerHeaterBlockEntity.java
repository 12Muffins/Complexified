package net.muffin.complexified.block.entity;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;

import com.simibubi.create.content.fluids.tank.BoilerData;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import joptsimple.internal.Strings;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.muffin.complexified.block.custom.BoilerHeaterBlock;
import net.muffin.complexified.block.custom.BoilerHeaterBlock.GrateCorner;
import net.muffin.complexified.foundation.block.behaviour.ConnectivityHandlerShort;
import net.muffin.complexified.foundation.lang.ComplexifiedLang;
import net.muffin.complexified.recipe.BoilerHeating;
import org.jetbrains.annotations.NotNull;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.Math.abs;

public class BoilerHeaterBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IMultiBlockEntityContainer.Fluid {
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final int MAX_SIZE = 3;

    protected LazyOptional<IFluidHandler> fluidCapability;
    protected HeaterFluidHandler tankInventory;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected boolean updateCapability;
    protected int width;

    private static final Object boilerHeatingRecipeKey = new Object();
    private net.minecraft.world.level.material.Fluid lastFluidIngredient;
    private net.minecraft.world.level.material.Fluid lastFluidResult;

    private static final int SYNC_RATE = 8;
    protected int syncCooldown;
    protected boolean queuedSync;

    public Heating heating;

    public BoilerHeaterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        heating = new Heating();
        tankInventory = createTankInventory();
        fluidCapability = LazyOptional.of(() -> tankInventory);
        updateConnectivity = false;
        updateCapability = false;
//        height = 1;
        width = 1;
        refreshCapability();
    }

    private HeaterFluidHandler createTankInventory() {
        return new HeaterFluidHandler(getCapacityMultiplier(), this::onFluidStackChanged);
    }


    public void updateConnectivity() {
        updateConnectivity = false;
        if (level.isClientSide)
            return;
        if (!isController())
            return;
        ConnectivityHandlerShort.formMulti(this);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (isController()) heating.updateOcclusion(this);
    }

    @Override
    public void tick() {
        super.tick();

        if (heating.updateHeat) {
            this.getHeatValue();
        }

        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync)
                sendData();
        }

        if (lastKnownPos == null)
            lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition) && worldPosition != null) {
            onPositionChanged();
            return;
        }

        if (updateCapability) {
            updateCapability = false;
            refreshCapability();
        }
        if (updateConnectivity)
            updateConnectivity();

        if (isController()) {
            heating.tick(this);
        }
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.getX() == controller.getX()
                && worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
    }

    @Override
    public void initialize() {
        super.initialize();
        sendData();
        if (level.isClientSide)
            invalidateRenderBoundingBox();
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = worldPosition;
    }

    protected void onFluidStackChanged(FluidStack newFluidStack) {
        if (!hasLevel())
            return;

        if (!level.isClientSide) {
            setChanged();
            sendData();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public BoilerHeaterBlockEntity getControllerBE() {
        if (isController() || !hasLevel())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof BoilerHeaterBlockEntity)
            return (BoilerHeaterBlockEntity) blockEntity;
        return null;
    }

    public void applyFluidTankSize(int blocks) {
        tankInventory.setCapacity(blocks * getCapacityMultiplier());
        int overflow =  tankInventory.getFluidAmount() - tankInventory.getCapacity();
        if (overflow > 0) tankInventory.drain(overflow, IFluidHandler.FluidAction.EXECUTE);
    }

    public void removeController(boolean keepFluids) {
        if (level.isClientSide)
            return;
        updateConnectivity = true;
        if (!keepFluids)
            applyFluidTankSize(1);
        controller = null;
        width = 1;

        onFluidStackChanged(tankInventory.getFluid());

        BlockState state = getBlockState();
        if (BoilerHeaterBlock.isHeater(state)) {
            state = state.setValue(BoilerHeaterBlock.HEATER_GRATE, GrateCorner.all);
            getLevel().setBlock(worldPosition, state, 22);
        }


        refreshCapability();
        setChanged();
        sendData();
    }

    public void sendDataImmediately() {
        syncCooldown = 0;
        queuedSync = false;
        sendData();
    }

    @Override
    public void sendData() {
        if (syncCooldown > 0) {
            queuedSync = true;
            return;
        }
        super.sendData();
        queuedSync = false;
        syncCooldown = SYNC_RATE;
    }

    public void setGrate() {
        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {

                BlockPos pos = this.worldPosition.offset(xOffset, 0, zOffset);
                BlockState blockState = level.getBlockState(pos);
                if (!BoilerHeaterBlock.isHeater(blockState))
                    continue;

                GrateCorner grate;
                if (width == 1)
                    grate = GrateCorner.all;

                else {
                    GrateCorner.Edge xEdge = GrateCorner.Edge.getEdge(xOffset, width);
                    GrateCorner.Edge zEdge = GrateCorner.Edge.getEdge(zOffset, width);

                    grate = GrateCorner.mergeEdge(xEdge, zEdge);
                }

                level.setBlock(pos, blockState.setValue(BoilerHeaterBlock.HEATER_GRATE, grate), 22);
                level.getChunkSource()
                        .getLightEngine()
                        .checkBlock(pos);
            }
        }
    }

    @Override
    public void setController(BlockPos controller) {
        if (level.isClientSide && !isVirtual())
            return;
        if (controller.equals(this.controller))
            return;
        this.controller = controller;
        refreshCapability();
        setChanged();
        sendData();
    }

    private void refreshCapability() {
        LazyOptional<IFluidHandler> oldCap = fluidCapability;
        fluidCapability = LazyOptional.of(this::handlerForCapability);
        oldCap.invalidate();
    }

    private IFluidHandler handlerForCapability() {
        return isController() ? tankInventory
                : getControllerBE() != null ? getControllerBE().handlerForCapability() : createHandler(0);
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        if (isController())
            return super.createRenderBoundingBox().expandTowards(width - 1, 0, width - 1);
        else
            return super.createRenderBoundingBox();
    }


    @javax.annotation.Nullable
    public BoilerHeaterBlockEntity getOtherBoilerHeaterBlockEntity(Direction direction) {
        BlockEntity otherBE = level.getBlockEntity(worldPosition.relative(direction));
        if (otherBE instanceof BoilerHeaterBlockEntity)
            return (BoilerHeaterBlockEntity) otherBE;
        return null;
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = width;

        updateConnectivity = compound.contains("Uninitialized");
        controller = null;
        lastKnownPos = null;

        if (compound.contains("LastKnownPos"))
            lastKnownPos = NbtUtils.readBlockPos(compound.getCompound("LastKnownPos"));
        if (compound.contains("Controller"))
            controller = NbtUtils.readBlockPos(compound.getCompound("Controller"));

        if (isController()) {
            width = compound.getInt("Size");
            tankInventory.setCapacity(getTotalTankSize() * getCapacityMultiplier());
            tankInventory.readFromNBT(compound.getCompound("TankContent"));
            if (tankInventory.getSpace() < 0)
                tankInventory.drain(-tankInventory.getSpace(), IFluidHandler.FluidAction.EXECUTE);
        }

        updateCapability = true;
        heating.read(compound.getCompound("Heater"));

        if (!clientPacket)
            return;

        boolean changeOfController = !Objects.equals(controllerBefore, controller);
        if (changeOfController || prevSize != width) {
            if (hasLevel())
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            if (isController())
                tankInventory.setCapacity(getCapacityMultiplier() * getTotalTankSize());
            invalidateRenderBoundingBox();
        }
    }

    public float getFillState() {
        return (float) tankInventory.getFluidAmount() / tankInventory.getCapacity();
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        compound.put("Heater", heating.write());
        compound.putBoolean("IsSupplier", heating.isHeatSupplier);
        if (updateConnectivity)
            compound.putBoolean("Uninitialized", true);
        if (lastKnownPos != null)
            compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            compound.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
            compound.put("TankContent", tankInventory.writeToNBT(new CompoundTag()));
            compound.putInt("Size", width);
        }
        super.write(compound, clientPacket);

        if (!clientPacket)
            return;
        if (queuedSync)
            compound.putBoolean("LazySync", true);
    }

    @Override
    public void writeSafe(CompoundTag compound) {
        if (isController()) {
            compound.putInt("Size", width);
        }
    }

    @Nonnull
    @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (!fluidCapability.isPresent())
            refreshCapability();
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return fluidCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
//        inputTank = createInputTank();
//        outputTank = createOutputTank();
//        behaviours.add(inputTank);
//        behaviours.add(outputTank);
    }

    private SmartFluidTank createInputTank() {
        return new SmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
    }

    private SmartFluidTank createOutputTank() {
            return new SmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
    }


    public FluidTank getTankInventory() {
        return tankInventory;
    }

    public int getTotalTankSize() {
        return width * width;
    }

    public static int getMaxSize() {
        return MAX_SIZE;
    }

    public static int getCapacityMultiplier() {
        return AllConfigs.server().fluids.fluidTankCapacity.get() * 1000;
    }

    public static int getMaxHeight() {
        return 3;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        BlockState state = this.getBlockState();
        if (BoilerHeaterBlock.isHeater(state)) { // safety
            level.setBlock(getBlockPos(), state, 6);
        }
        if (isController())
            setGrate();
        onFluidStackChanged(tankInventory.getFluid());
        setChanged();
    }

    @Override
    public void setExtraData(@javax.annotation.Nullable Object data) {
        if (data instanceof Boolean)
            return;
    }

    @Override
    @Nullable
    public Object getExtraData() {
        return true;
    }

    @Override
    public Object modifyExtraData(Object data) {
//        if (data instanceof Boolean windows) {
//            windows |= window;
//            return windows;
//        }
        return data;
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return Direction.Axis.Y;
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        if (longAxis == Direction.Axis.Y)
            return getMaxHeight();
        return getMaxWidth();
    }

    @Override
    public int getMaxWidth() {
        return MAX_SIZE;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public void setHeight(int height) {}

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public boolean hasTank() {
        return true;
    }

    @Override
    public int getTankSize(int tank) {
        return getCapacityMultiplier();
    }

    @Override
    public void setTankSize(int tank, int blocks) {
        applyFluidTankSize(blocks);
    }

    @Override
    public IFluidTank getTank(int tank) {
        return tankInventory;
    }

    @Override
    public FluidStack getFluid(int tank) {
        return tankInventory.getFluid()
                .copy();
    }

    public HeaterFluidHandler createHandler(int capacity) {
        return new HeaterFluidHandler(capacity, this::onFluidStackChanged);
    }

    protected List<Recipe<?>> getMatchingRecipes(FluidStack resource) {
        if (getControllerBE() == null) return new ArrayList<>();

        List<Recipe<?>> list = new ArrayList<>();
        for (Recipe<?> r : RecipeFinder.get(getRecipeCacheKey(), level, this::matchStaticFilters))
            if (matchBoilerHeaterRecipe(r, resource))
                list.add(r);

        list.sort((r1, r2) -> r2.getIngredients().size() - r1.getIngredients().size());

        return list;
    }

    protected <C extends Container> boolean matchBoilerHeaterRecipe(Recipe<?> recipe, FluidStack resource) {
        if (recipe == null)
            return false;
        BoilerHeaterBlockEntity boilerHeater = getControllerBE();

        if (recipe instanceof BoilerHeating heatingRecipe) {
            net.minecraft.world.level.material.Fluid fluidIngredient = heatingRecipe.getFluidForIngredient();
            FluidStack fluidResulting = heatingRecipe.getResultingFluid();
            boolean isValid = fluidIngredient.equals(resource.getFluid());
            isValid &= boilerHeater.tankInventory.testFill(fluidResulting, IFluidHandler.FluidAction.SIMULATE) != 0;
            return isValid;
        }
        return false;
    }


    protected <C extends Container> boolean matchStaticFilters(Recipe<C> r) {
        return r instanceof BoilerHeating;
    }

    public Object getRecipeCacheKey() {return boilerHeatingRecipeKey;}

    public class Heating {
        protected boolean isHeatSupplier;
        protected int boilerTanks;

        float[] supplyOverTime = new float[10];

        public static final int SAMPLE_RATE = 5;
        public int ticksUntilNextSample;
        public int currentHeat;
        public int currentIndex;
        public int gatheredSupply;
        private boolean updateHeat;

        public float heatingFluidSupply;

        // display only
        int maxValue;
        int minValue;
        public boolean[] occludedDirections = {true, true, true, true};

        public static final double REQUIRED_RATE = 20;

        public LerpedFloat gauge = LerpedFloat.linear();

        public CompoundTag write() {
            CompoundTag nbt = new CompoundTag();
            nbt.putFloat("Supply", heatingFluidSupply);
            nbt.putInt("CurrentHeat", currentHeat);
            return nbt;
        }

        public void read(CompoundTag nbt) {
            heatingFluidSupply = nbt.getFloat("Supply");
            currentHeat = nbt.getInt("CurrentHeat");
            Arrays.fill(supplyOverTime, currentHeat);

            gauge.chase((double) currentHeat / (2 * getTotalTankSize()), 0.125f, LerpedFloat.Chaser.EXP);
        }

        public void tick(BoilerHeaterBlockEntity controller) {
            Level level = controller.getLevel();
            if (level.isClientSide) {
                gauge.tickChaser();
                float current = gauge.getValue(1);
                if (current > 1 && level.random.nextFloat() < 1 / 2f)
                    gauge.setValueNoUpdate(current + Math.min(-(current - 1) * level.random.nextFloat(), 0));
                return;
            }

            ticksUntilNextSample--;
            if (ticksUntilNextSample > 0)
                return;

            ticksUntilNextSample = SAMPLE_RATE;
            supplyOverTime[currentIndex] = gatheredSupply / (float) SAMPLE_RATE;
            heatingFluidSupply = Math.max(heatingFluidSupply, supplyOverTime[currentIndex]);
            currentIndex = (currentIndex + 1) % supplyOverTime.length;
            gatheredSupply = 0;

            if (currentIndex == 0) {
                heatingFluidSupply = 0;
                for (float i : supplyOverTime) {
                    heatingFluidSupply = Math.max(i, heatingFluidSupply);
                }
            }

            int prevHeat = currentHeat;
            int totalTankSize = controller.getTotalTankSize();
            currentHeat = Math.min( (int) (heatingFluidSupply / (REQUIRED_RATE)), 2 * totalTankSize); // sets heat to a maximum of 18
            if (currentHeat != prevHeat) {
                updateHeat = true;
            }
            controller.notifyUpdate();
        }

        public void updateOcclusion(BoilerHeaterBlockEntity controller) {
            if (!controller.getLevel().isClientSide)
                return;
            for (Direction d : Iterate.horizontalDirections) {
                AABB aabb =
                        new AABB(controller.getBlockPos()).move(controller.width / 2f - .5f, 0, controller.width / 2f - .5f)
                                .deflate(5f / 8);
                aabb = aabb.move(d.getStepX() * (controller.width / 2f + 1 / 4f), 0,
                        d.getStepZ() * (controller.width / 2f + 1 / 4f));
                aabb = aabb.inflate(Math.abs(d.getStepZ()) / 2f, 0.25f, Math.abs(d.getStepX()) / 2f);
                occludedDirections[d.get2DDataValue()] = !controller.getLevel()
                        .noCollision(aabb);
            }
        }

        protected int getHeat(BoilerHeaterBlockEntity heater) {
            updateHeatSupplier(heater);
            if (updateHeat) {
                heater.heating.updateHeatSupplier(heater);
            }
            updateHeat = false;
            if (isHeatSupplier)
                return Math.min(boilerTanks * currentHeat / getTotalTankSize() , 2 * boilerTanks); //`9` is the max Size for the heater
            return BoilerHeater.NO_HEAT;
        }

        private FluidTankBlockEntity getBoiler(BlockGetter level, BlockPos pos) {
            if (level.getBlockEntity(pos) instanceof FluidTankBlockEntity fluidTank) {
                return fluidTank;
            };
            return null;
        }

        public void updateHeatSupplier(BoilerHeaterBlockEntity heater) {
            BlockPos heaterPos = heater.worldPosition;
            FluidTankBlockEntity boiler = getBoiler(level, heaterPos.above());
            if (boiler == null) {
                return;
            }

            BlockPos boilerController = boiler.getController();
            int boilerCX = boilerController.getX();
            int boilerCZ = boilerController.getZ();
            int widthB = boiler.getControllerBE().getWidth();
            BlockPos heaterController = heater.getController();
            int heaterCX = heaterController.getX();
            int heaterCZ = heaterController.getZ();
            int widthH = heater.getWidth();

            int diffX = boilerCX - heaterCX;
            int diffZ = boilerCZ - heaterCZ;

            int heatedArea = Math.max(0, Math.min(widthH, diffX + widthB) - Math.max(0, diffX))
                    * Math.max(0, Math.min(widthH, diffZ + widthB) - Math.max(0, diffZ));

            if (Math.max(0, diffX) == heaterPos.getX() - heaterCX && Math.max(0, diffZ) == heaterPos.getZ() - heaterCZ) {
                boilerTanks = heatedArea;
                isHeatSupplier = true;
                boiler.getControllerBE().updateBoilerTemperature();
            } else {
                boilerTanks = 0;
                isHeatSupplier = false;
            }
        }

        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, HeaterFluidHandler tankInventory) {
            double actualFluidRate = REQUIRED_RATE;

            maxValue = Math.max(currentHeat, (int) (heatingFluidSupply / (actualFluidRate)));
            minValue = Math.min(currentHeat, (int) (heatingFluidSupply / actualFluidRate));

            FluidStack heatingFluidType = tankInventory.getFluidInTank(0);

            ComplexifiedLang.translate("heater.status", getHeaterStatus().withStyle(ChatFormatting.GREEN))
                    .forGoggles(tooltip);

            ComplexifiedLang.builder().add(getSizeComponent(true, false)).forGoggles(tooltip, 1);

            ComplexifiedLang.builder().add(getHeatComponent(true, false)).add(
                    ComplexifiedLang.translate("util.in_bracket", ComplexifiedLang.number(currentHeat))
                            .style(ChatFormatting.DARK_GRAY))
            .forGoggles(tooltip, 1);

            ComplexifiedLang.builder().add(getThroughPutComponent(true, false))
//                    .add(
//                    ComplexifiedLang.translate("util.in_bracket",
//                            ComplexifiedLang.fluidName(heatingFluidType)).style(ChatFormatting.DARK_GRAY)
//                    )
            .forGoggles(tooltip, 1);

            if (currentHeat == 0) {
                tooltip.add(CommonComponents.EMPTY);

                ComplexifiedLang.translate("heater.unsufficient_rate")
                        .style(ChatFormatting.GRAY)
                        .forGoggles(tooltip);
                ComplexifiedLang.number(heatingFluidSupply)
                        .style(ChatFormatting.GOLD)
                        .add(CreateLang.translate("generic.unit.millibuckets"))
                        .add(ComplexifiedLang.text(" / ").style(ChatFormatting.GRAY))
                        .add(ComplexifiedLang.translate("heater.per_tick", ComplexifiedLang.number(Math.ceil(REQUIRED_RATE))
                                        .add(CreateLang.translate("generic.unit.millibuckets")))
                                .style(ChatFormatting.DARK_GRAY))
                        .forGoggles(tooltip, 1);
                if (!tankInventory.isFull())
                    return true;

                tooltip.add(CommonComponents.EMPTY);
                FluidStack fluidStack = tankInventory.getFluidInTank(0);
                LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");

                ComplexifiedLang.fluidName(fluidStack)
                        .style(ChatFormatting.GRAY)
                        .forGoggles(tooltip, 0);

                ComplexifiedLang.builder()
                        .add(ComplexifiedLang.number(fluidStack.getAmount())
                                .add(mb)
                                .style(ChatFormatting.GOLD))
                        .text(ChatFormatting.GRAY, " / ")
                        .add(ComplexifiedLang.number(tankInventory.getTankCapacity(0))
                                .add(mb)
                                .style(ChatFormatting.DARK_GRAY))
                        .forGoggles(tooltip, 1);

            }

            return true;
        }

        public MutableComponent getHeaterSize() {
            return switch (width) {
                case 1 -> ComplexifiedLang.translateDirect("heater.size.small");
                case 2 -> ComplexifiedLang.translateDirect("heater.size.medium");
                default -> ComplexifiedLang.translateDirect("heater.size.large");
            };
        }

        public MutableComponent getHeaterStatus() {
            if (currentHeat <= 0)
                return ComplexifiedLang.translateDirect("heater.inactive");
            else if (currentHeat < 18)
                return ComplexifiedLang.translateDirect("heater.active");
            else
                return ComplexifiedLang.translateDirect("heater.superheated");
        }

        public MutableComponent getSizeComponent(boolean forGoggles, boolean useBlocksAsBars, ChatFormatting... styles) {
            return ComplexifiedLang.translateDirect("heater.size")
                    .withStyle(ChatFormatting.GRAY)
                    .append(ComplexifiedLang.translateDirect("heater.size_dots")
                    .withStyle(ChatFormatting.DARK_GRAY))
                    .append(getHeaterSize().withStyle(ChatFormatting.GREEN));

        }

        public MutableComponent getHeatComponent(boolean forGoggles, boolean useBlocksAsBars, ChatFormatting... styles) {
            return componentHelper("heat", currentHeat, forGoggles, useBlocksAsBars, styles);
        }

        public MutableComponent getThroughPutComponent(boolean forGoggles, boolean useBlocksAsBars, ChatFormatting... styles) {
//            LOGGER.info("level (rate): {}", heatingFluid);
            return componentHelper("flow_rate", (int) (heatingFluidSupply / (REQUIRED_RATE)), forGoggles, useBlocksAsBars, styles);
        }

        private MutableComponent componentHelper(String label, int level, boolean forGoggles, boolean useBlocksAsBars, ChatFormatting... styles) {
            MutableComponent base = useBlocksAsBars ? blockComponent(level) : barComponent(level);

            if (!forGoggles)
                return base;

            ChatFormatting style1 = styles.length >= 1 ? styles[0] : ChatFormatting.GRAY;
            ChatFormatting style2 = styles.length >= 2 ? styles[1] : ChatFormatting.DARK_GRAY;

            return ComplexifiedLang.translateDirect("heater." + label)
                    .withStyle(style1)
                    .append(ComplexifiedLang.translateDirect("heater." + label + "_dots")
                            .withStyle(style2))
                    .append(base);
        }

        private MutableComponent blockComponent(int level) {
            return Component.literal("" + "\u2588".repeat(minValue) + "\u2592".repeat(level - minValue) + "\u2591".repeat(maxValue - level));
        }

        private MutableComponent barComponent(int level) {
            return Component.empty()
                    .append(bars(Math.max(0, minValue - 1), ChatFormatting.DARK_GREEN))
                    .append(bars(minValue > 0 ? 1 : 0, ChatFormatting.GREEN))
                    .append(bars(Math.max(0, level - minValue), ChatFormatting.DARK_GREEN))
                    .append(bars(Math.max(0, maxValue - level), ChatFormatting.DARK_RED))
                    .append(bars(Math.max(0, Math.min(18 - maxValue, ((maxValue / 5 + 1) * 5) - maxValue)),
                            ChatFormatting.DARK_GRAY));
        }
        private MutableComponent bars(int level, ChatFormatting format) {
            return Component.literal(Strings.repeat('|', level))
                    .withStyle(format);
        }
    }

    public int getHeat() {
        return this.heating.getHeat(this);
    }

    public int getHeatValue() {
        if (level == null) {
            return BoilerHeater.NO_HEAT;
        }
        return getHeat();
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        BoilerHeaterBlockEntity controllerBE = getControllerBE();
        if (controllerBE == null)
            return false;
        if (isPlayerSneaking && level.getBlockEntity(getController().above()) instanceof FluidTankBlockEntity fluidTank) {
            if (fluidTank.getControllerBE().boiler.addToGoggleTooltip(tooltip, isPlayerSneaking, fluidTank.getControllerBE().getTotalTankSize())) {
                return true;
            }
        }

        if (!controllerBE.heating.addToGoggleTooltip(tooltip, isPlayerSneaking, controllerBE.tankInventory)) {
            return false;
        }
        return true;
    }

    public class HeaterFluidHandler extends SmartFluidTank implements IFluidHandler {

        public HeaterFluidHandler(int capacity, Consumer<FluidStack> updateCallback) {
            super(capacity, updateCallback);
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return fluid;
        }

        @Override
        public int getTankCapacity(int tank) {
            return super.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!isFluidValid(resource))
                return 0;
            net.minecraft.world.level.material.Fluid fluidIngredient = resource.getFluid();
            if (lastFluidIngredient != fluidIngredient) {
                List<Recipe<?>> recipes = getMatchingRecipes(resource);
                if (recipes.isEmpty()) return super.fill(resource, action);
                BoilerHeating recipe = (BoilerHeating) recipes.get(0);
                lastFluidIngredient = recipe.getFluidForIngredient();
                lastFluidResult = recipe.getResultingFluid().getFluid();
            }

            int amount = super.fill(new FluidStack(lastFluidResult, resource.getAmount()), action);
            if (action.execute())
                heating.gatheredSupply += amount;
            return amount;
        }

        protected int testFill(FluidStack resource, FluidAction action) {
            return super.fill(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return super.drain(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return super.drain(maxDrain, action);
        }

        public boolean isFull() {
            return this.capacity <= this.fluid.getAmount();
        }
    }
}
