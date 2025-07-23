package net.muffin.complexified.block.entity;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.simple.DeferralBehaviour;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import joptsimple.internal.Strings;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Iterate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.muffin.complexified.block.ModBlocks;
import net.muffin.complexified.block.custom.BoilerHeaterBlockOld;
import net.muffin.complexified.block.custom.BoilerHeaterStructuralBlock;
import net.muffin.complexified.foundation.blockEntity.behaviour.fluid.SmartFilteredTankBehaviour;
import net.muffin.complexified.foundation.lang.ComplexifiedLang;
import net.muffin.complexified.recipe.BoilerHeating;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BoilerHeaterBlockEntityOld extends SmartBlockEntity implements IHaveGoggleInformation {

    public SmartFilteredTankBehaviour inputTank;
    protected SmartFilteredTankBehaviour outputTank;
    private boolean contentsChanged;
    protected LazyOptional<IFluidHandler> fluidCapability;

    protected BlockPos controller;
    protected BlockPos lastKnownPos;

    protected DeferralBehaviour coolFluid;


    protected BoilerHeating currentRecipe;
    protected int recipeScaler;
    public int processingTicks;
    private static final Object boilerHeatingRecipeKey = new Object();

    private boolean updateHeat;

    public final BoilerInfo boilerInfo;
    public HeatInfo heatInfo;
    private boolean heatTicked;

    public class HeatInfo {
        float[] supplyOverTime = new float[10];

        public static final int SAMPLE_RATE = 5;
        public int ticksUntilNextSample;
        public int currentHeat;
        public int currentIndex;
        public int gatheredSupply;

        public float heatingFluid;

        // display only
        int maxValue;
        int minValue;
        public boolean[] occludedDirections = {true, true, true, true};

        public static final int REQUIRED_RATE = 12;

        public LerpedFloat gauge = LerpedFloat.linear();


        public CompoundTag write() {
            CompoundTag nbt = new CompoundTag();
            nbt.putFloat("Supply", heatingFluid);
            nbt.putInt("CurrentHeat", currentHeat);
            return nbt;
        }

        public void read(CompoundTag nbt) {
            heatingFluid = nbt.getFloat("Supply");
            currentHeat = nbt.getInt("CurrentHeat");
            Arrays.fill(supplyOverTime, currentHeat);

            gauge.chase(currentHeat / 18f, 0.125f, LerpedFloat.Chaser.EXP);
        }


        public void tick(BoilerHeaterBlockEntityOld controller) {
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
            heatingFluid = Math.max(heatingFluid, supplyOverTime[currentIndex]);
            currentIndex = (currentIndex + 1) % supplyOverTime.length;
            gatheredSupply = 0;

            if (currentIndex == 0) {
                heatingFluid = 0;
                for (float i : supplyOverTime) {
                    heatingFluid = Math.max(i, heatingFluid);
                }
            }

            int prevHeat = currentHeat;
            currentHeat = Math.min(Mth.floor((heatingFluid / REQUIRED_RATE)), 18); // sets heat to a maximum of 18
            if (currentHeat != prevHeat) {
                controller.updateHeat = true;
            }
            controller.notifyUpdate();
        }

        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, SmartFluidTankBehaviour.TankSegment primaryTank) {

            maxValue = Math.max(currentHeat, (int) heatingFluid / REQUIRED_RATE);
            minValue = Math.min(currentHeat, (int) heatingFluid / REQUIRED_RATE);

            FluidStack heatingFluidType = primaryTank.getRenderedFluid();

            ComplexifiedLang.translate("heater.status", getHeaterStatus().withStyle(ChatFormatting.GREEN))
                    .forGoggles(tooltip);

            ComplexifiedLang.builder().add(getHeatComponent()).add(
                    ComplexifiedLang.translate("util.in_bracket",
                    ComplexifiedLang.number(currentHeat)).style(ChatFormatting.DARK_GRAY)
            ).forGoggles(tooltip, 1);

            ComplexifiedLang.builder().add(getThroughPutComponent()).add(
                    ComplexifiedLang.translate("util.in_bracket",
                    ComplexifiedLang.fluidName(heatingFluidType)).style(ChatFormatting.DARK_GRAY)
            ).forGoggles(tooltip, 1);

            return true;
        }

        public void updateOcclusion(BoilerHeaterBlockEntityOld controller) {
            Level ControllerLevel = controller.getLevel();
            assert ControllerLevel != null;
            for (Direction d : Iterate.horizontalDirections) {
                AABB aabb =
                        new AABB(controller.getBlockPos())
                                .deflate(5f / 8);
                aabb = aabb.move(d.getStepX() * (3 / 2f + 1 / 4f), 0,
                        d.getStepZ() * (3 / 2f + 1 / 4f));
                aabb = aabb.inflate(Math.abs(d.getStepZ()) / 2f, 0.25f, Math.abs(d.getStepX()) / 2f);
                occludedDirections[d.get2DDataValue()] = !ControllerLevel
                        .noCollision(aabb);
            }
        }

        public MutableComponent getHeaterStatus() {
            if (currentHeat <= 0)
                return ComplexifiedLang.translateDirect("heater.inactive");
            else if (currentHeat < 18)
                return ComplexifiedLang.translateDirect("heater.active");
            else
                return ComplexifiedLang.translateDirect("heater.superheated");
        }

        public MutableComponent getHeatComponent(ChatFormatting... styles) {
//            LOGGER.info("level (heat): {}", currentHeat);
            return componentHelper("heat", currentHeat, styles);
        }

        public MutableComponent getThroughPutComponent(ChatFormatting... styles) {
//            LOGGER.info("level (rate): {}", heatingFluid);
            return componentHelper("flow_rate", (int) heatingFluid / REQUIRED_RATE, styles);
        }

        private MutableComponent componentHelper(String label, int level, ChatFormatting... styles) {
            MutableComponent base = barComponent(level);

            ChatFormatting style1 = styles.length >= 1 ? styles[0] : ChatFormatting.GRAY;
            ChatFormatting style2 = styles.length >= 2 ? styles[1] : ChatFormatting.DARK_GRAY;

            return ComplexifiedLang.translateDirect("heater." + label)
                    .withStyle(style1)
                    .append(ComplexifiedLang.translateDirect("heater." + label + "_dots")
                            .withStyle(style2))
                    .append(base);
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

    protected class BoilerInfo {
        protected boolean isHeatSupplier;
        protected int boilerTanks;

//        public int getHeat(BoilerHeaterBlockEntityOld heater) {
//            if (updateHeat) {
//            heater.boilerInfo.updateHeatSupplier(heater);
//            }
//            updateHeat = false;
//            if (isHeatSupplier)
//                return Math.min(boilerTanks * heatInfo.currentHeat / 9, 2 * boilerTanks);
//            return BoilerHeater.NO_HEAT;
//        }

        public int getHeat() {
            boilerInfo.updateHeatSupplier();
            if (isHeatSupplier) {
                BlockEntity possibleHeater = level.getBlockEntity(controller);
                if (possibleHeater instanceof BoilerHeaterBlockEntityOld heaterController) {
                    int heat = Math.min(boilerTanks * heaterController.heatInfo.currentHeat / 9, 2 * boilerTanks);
                    return heat == 0 ? BoilerHeater.NO_HEAT : heat;
                }            }
            return BoilerHeater.NO_HEAT;
        }

        public void updateHeatSupplier(BlockGetter level, BlockPos heaterPos) {
            FluidTankBlockEntity boiler = getBoiler(level, heaterPos, 1);

            if (boiler == null) {
                assert level != null;
                return;
            }

            BlockPos boilerController = boiler.getController();
            int boilerCX = boilerController.getX();
            int boilerCZ = boilerController.getZ();
            int width = boiler.getControllerBE().getWidth();
            BlockPos heaterController = getController();
            int heaterCX = heaterController.getX()-1;
            int heaterCZ = heaterController.getZ()-1;

            int diffX = boilerCX - heaterCX;
            int diffZ = boilerCZ - heaterCZ;

            int heatedArea = Math.max(0, Math.min(3, diffX + width) - Math.max(0, diffX))
                    * Math.max(0, Math.min(3, diffZ + width) - Math.max(0, diffZ));

            if (Math.max(0, diffX) == heaterPos.getX() - heaterCX && Math.max(0, diffZ) == heaterPos.getZ() - heaterCZ) {
                boilerTanks = heatedArea;
                isHeatSupplier = true;
                boiler.getControllerBE().updateBoilerTemperature();
            } else {
                boilerTanks = 0;
                isHeatSupplier = false;
            }
        }

        public void updateHeatSupplier() {
            updateHeatSupplier(level, worldPosition);
        }

//        public void updateHeatSupplier(BoilerHeaterBlockEntityOld heater) {
//            BlockPos heaterPos = heater.worldPosition;
//            FluidTankBlockEntity boiler = getBoiler(heater.level, heaterPos, 1);
//            if (boiler == null) {
//                assert heater.level != null;
//                return;
//            }
//
//            BlockPos boilerController = boiler.getController();
//            int boilerCX = boilerController.getX();
//            int boilerCZ = boilerController.getZ();
//            int width = boiler.getControllerBE().getWidth();
//            BlockPos heaterController = heater.getController();
//            int heaterCX = heaterController.getX()-1;
//            int heaterCZ = heaterController.getZ()-1;
//
//            int diffX = boilerCX - heaterCX;
//            int diffZ = boilerCZ - heaterCZ;
//
//            int heatedArea = Math.max(0, Math.min(3, diffX + width) - Math.max(0, diffX))
//                    * Math.max(0, Math.min(3, diffZ + width) - Math.max(0, diffZ));
//
//            if (Math.max(0, diffX) == heaterPos.getX() - heaterCX && Math.max(0, diffZ) == heaterPos.getZ() - heaterCZ) {
//                boilerTanks = heatedArea;
//                isHeatSupplier = true;
//            } else {
//                boilerTanks = 0;
//                isHeatSupplier = false;
//            }
//        }

        private FluidTankBlockEntity getBoiler(BlockGetter level, BlockPos pos, int height) {
            return getBoiler(level, pos.above(height));
        }

        private FluidTankBlockEntity getBoiler(BlockGetter level, BlockPos pos) {
            if (level.getBlockEntity(pos) instanceof FluidTankBlockEntity fluidTank) {
                return fluidTank;
            };
            return null;
        }
    }

    public static final Logger LOGGER = LogUtils.getLogger();


    public BoilerHeaterBlockEntityOld(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        contentsChanged = true;
//        tanks = Couple.create(getControllerBE().inputTank, getControllerBE().outputTank);
        boilerInfo = new BoilerInfo();
        heatInfo = new HeatInfo();
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        compound.put("Heater", heatInfo.write());
        if (lastKnownPos != null)
            compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            compound.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {

        }
            super.write(compound, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if (compound.contains("Controller")) {
            controller = NbtUtils.readBlockPos(compound.getCompound("Controller"));
        } else controller = worldPosition;
        heatInfo.read(compound.getCompound("Heater"));

    }

    @Override
    public void writeSafe(CompoundTag compound) {
//        if (isController());
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
            inputTank = (SmartFilteredTankBehaviour) new SmartFilteredTankBehaviour(SmartFilteredTankBehaviour.INPUT, this,
                    1, 24000, true, Fluids.LAVA)
                    .whenFluidUpdates(() -> {
                        contentsChanged = true;
                        coolFluid.scheduleUpdate();
                    });

            outputTank = (SmartFilteredTankBehaviour) new SmartFilteredTankBehaviour(SmartFilteredTankBehaviour.OUTPUT, this,
                    1, 24000, true)
                    .whenFluidUpdates(() -> {
                        contentsChanged = true;
                    })
                    .forbidInsertion();

        coolFluid = new DeferralBehaviour(getControllerBE(), this::updateBoilerHeater);
            behaviours.add(inputTank);
            behaviours.add(outputTank);
            behaviours.add(coolFluid);

        fluidCapability = LazyOptional.of(() -> {
            LazyOptional<? extends IFluidHandler> inputCap = getControllerBE().inputTank.getCapability();
            LazyOptional<? extends IFluidHandler> outputCap = getControllerBE().outputTank.getCapability();
            return new CombinedTankWrapper(outputCap.orElse(null), inputCap.orElse(null));
        });
    }

    @Override
    public void invalidate() {
        super.invalidate();
        sendData();
        fluidCapability.invalidate();
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return fluidCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (isController()) heatInfo.updateOcclusion(this);
//        if (!level.isClientSide) {
//            updateSpoutput();
//            if (recipeBackupCheck-- > 0)
//                return;
//            recipeBackupCheck = 20;
//            if (isEmpty())
//                return;
//            notifyChangeOfContents();
//            return;
//        }
    }

    private boolean isEmpty() {
        return inputTank.isEmpty() && outputTank.isEmpty();
    }

    public BlockPos getController() { return isController() ? worldPosition : controller; }

    public BoilerHeaterBlockEntityOld getControllerBE() {
        if (isController() || !hasLevel())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof BoilerHeaterBlockEntityOld)
            return (BoilerHeaterBlockEntityOld) blockEntity;
        return null;
    }

    public boolean isController() {
        return controller == null || (worldPosition.getY() == controller.getY() && worldPosition.getX() == controller.getX()
                && controller.getZ() == worldPosition.getZ());
    }

    public void setController(BlockPos pos) {

        if (level.isClientSide && !isVirtual())
            return;
        if (pos.equals(this.controller))
            return;

        Block heater = level.getBlockState(pos).getBlock();
        if (heater instanceof BoilerHeaterStructuralBlock) {
            setController(BoilerHeaterStructuralBlock.getMaster(level, pos, level.getBlockState(pos)));
        } else if (heater instanceof BoilerHeaterBlockOld) {
            controller = pos;
        } else return;
        setChanged();
        sendData();
    }

    public void updateConnectivity() {
        if (level.isClientSide)
            return;
        if (!isController())
            return;
        setController(this.worldPosition);
    }

    @Override
    public void initialize() {
        super.initialize();
        sendData();
    }


    @Override
    public void tick() {
        super.tick();

        if (updateHeat) {
            this.getHeatValue();
        }
        if (!this.isController()) return;

        heatTicked = false;

        if (!level.isClientSide || isVirtual()) {
            if (processingTicks < 0) {
                float recipeSpeed = 1;
                if (currentRecipe != null) {
                int t = currentRecipe.getProcessingDuration();
                if (t != 0)
                    recipeSpeed = t / 100f;
                }

                processingTicks = Mth.clamp( Mth.floor(recipeSpeed + 1), 1, 512);

            } else {
                processingTicks--;
                if (processingTicks == 0) {
                    processingTicks = -1;
                    applyBoilerHeatingRecipe();
                    heatTicked = true;
                    sendData();
                }
            }
        }
        heatInfo.tick(this);
    }

    protected boolean updateBoilerHeater() {
        if (level == null || level.isClientSide)
            return true;

        BoilerHeaterBlockEntityOld boilerHeater = getControllerBE();

        if (boilerHeater == null) return false;
        if (!boilerHeater.getBlockState().is(ModBlocks.BOILER_HEATER_OLD.get())) return true;

        List<Recipe<?>> recipes = getMatchingRecipes();
        if (recipes.isEmpty())
            return true;
        currentRecipe = (BoilerHeating) recipes.get(0);
        sendData();
        return true;
    }

    protected List<Recipe<?>> getMatchingRecipes() {
        if (!getControllerBE().getBlockState().is(ModBlocks.BOILER_HEATER_OLD.get())) return new ArrayList<>();

        List<Recipe<?>> list = new ArrayList<>();
        for (Recipe<?> r : RecipeFinder.get(getRecipeCacheKey(), level, this::matchStaticFilters))
            if (matchBoilerHeaterRecipe(r))
                list.add(r);

        list.sort((r1, r2) -> r2.getIngredients().size() - r1.getIngredients().size());

        return list;
    }

    public int getRecipeScaler() {
        return recipeScaler;
    }

    public void setRecipeScaler(int recipeScaler) {
        this.recipeScaler = recipeScaler;
    }

    protected <C extends Container> boolean matchBoilerHeaterRecipe(Recipe<?> recipe) {
        if (recipe == null)
            return false;
        BoilerHeaterBlockEntityOld boilerHeater = getControllerBE();
        if (!boilerHeater.getBlockState().is(ModBlocks.BOILER_HEATER_OLD.get())) return false;

        if (recipe instanceof BoilerHeating heatingRecipe)
            return BoilerHeating.match(boilerHeater, heatingRecipe);
        return false;
    }



    protected <C extends Container> boolean matchStaticFilters(Recipe<C> r) {
        return true;
    }


    public Object getRecipeCacheKey() {return boilerHeatingRecipeKey;}


    protected void applyBoilerHeatingRecipe() {
        if (currentRecipe == null) {
            return;
        }

        BoilerHeaterBlockEntityOld boilerHeater = getControllerBE();
        if (!boilerHeater.getBlockState().is(ModBlocks.BOILER_HEATER_OLD.get())) return;

        if (!BoilerHeating.apply(boilerHeater, currentRecipe)) {
            heatInfo.gatheredSupply = 0;
            return;
        }
        boilerHeater.inputTank.sendDataImmediately();

        // Apply consumed liquid
        heatInfo.gatheredSupply = recipeScaler * currentRecipe.getFluidIngredients().stream().mapToInt(FluidIngredient::getRequiredAmount).sum();

//        LOGGER.info("gatheredSupply: " + heatInfo.gatheredSupply);
//        LOGGER.info("recipeScaler: " + recipeScaler);

        if (matchBoilerHeating(currentRecipe)) {
            sendData();
        }

        boilerHeater.notifyChangeOfContents();
    }

    protected boolean matchBoilerHeating(BoilerHeating recipe) {
        if (recipe == null)
            return false;
        BoilerHeaterBlockEntityOld boilerHeater = getControllerBE();
        if (!boilerHeater.getBlockState().is(ModBlocks.BOILER_HEATER_OLD.get())) return false;
        return BoilerHeating.match(boilerHeater, recipe);
    }


    public void notifyChangeOfContents() {
        contentsChanged = true;
    }


    public boolean acceptOutputs(List<FluidStack> outputFluids, boolean simulate) {
        //TODO ADD SCALAR
        getControllerBE().outputTank.allowInsertion();
        boolean acceptOutputsInner = acceptOutputsInner(outputFluids, simulate);
        getControllerBE().outputTank.forbidInsertion();
        return acceptOutputsInner;
    }

    private boolean acceptOutputsInner(List<FluidStack> outputFluids, boolean simulate) {
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof BoilerHeaterBlockOld))
            return false;
        IFluidHandler targetTank = getControllerBE().outputTank.getCapability()
                .orElse(null);

        if (outputFluids.isEmpty())
            return true;
        if (targetTank == null)
            return false;

        return acceptFluidOutputsIntoHeater(outputFluids, simulate, targetTank);
    }

    private boolean acceptFluidOutputsIntoHeater(List<FluidStack> outputFluids, boolean simulate, IFluidHandler targetTank) {
        if (simulate) {
            for (FluidStack fluidStack : outputFluids) {
                for (int t = 0; t < targetTank.getTanks(); t++) {
                    FluidStack targetFluid = targetTank.getFluidInTank(t);
                    if (targetFluid.getFluid().equals(fluidStack.getFluid())) {
                        int availableCapacity = targetTank.getTankCapacity(t) - targetFluid.getAmount();
                        recipeScaler = Mth.floor(Math.min((float) availableCapacity / fluidStack.getAmount(), recipeScaler));
                        recipeScaler = Math.max(recipeScaler, 1);
                    }
                }
            }
        }
        // Scale the output fluids based on the recipeScaler
        outputFluids = outputFluids.stream().map(f -> {
            FluidStack scaledF = f.copy();
            scaledF.setAmount(recipeScaler * f.getAmount());
            return scaledF;
        }).collect(Collectors.toList());

        // Attempt to fill the targetTank with the scaled fluids
        IFluidHandler.FluidAction action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
        for (FluidStack fluidStack : outputFluids) {
            int fill = targetTank instanceof SmartFilteredTankBehaviour.InternalFluidHandler
                    ? ((SmartFilteredTankBehaviour.InternalFluidHandler) targetTank).forceFill(fluidStack.copy(), action)
                    : targetTank.fill(fluidStack.copy(), action);
            if (fill != fluidStack.getAmount())
                return false;
        }

        return true;
    }

    // TODO WHY TF IS IT ALWAYS ZERO
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        BoilerHeaterBlockEntityOld controllerBE = getControllerBE();
        if (controllerBE == null)
            return false;
        if (isPlayerSneaking && level.getBlockEntity(getController().above()) instanceof FluidTankBlockEntity fluidTank) {
            if (fluidTank.getControllerBE().boiler.addToGoggleTooltip(tooltip, isPlayerSneaking, fluidTank.getControllerBE().getTotalTankSize())) {
                return true;
            }
        }

        if (!controllerBE.heatInfo.addToGoggleTooltip(tooltip, isPlayerSneaking, controllerBE.inputTank.getPrimaryTank())) {
            return false;
        }
        return true;
    }

    public int getHeatValue() {
        if (level == null) {
            return BoilerHeater.NO_HEAT;
        }
        return boilerInfo.getHeat();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).inflate(2);
    }

}
