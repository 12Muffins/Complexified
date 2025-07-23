package net.muffin.complexified.recipe;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.createmod.catnip.data.Iterate;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.muffin.complexified.block.entity.BoilerHeaterBlockEntityOld;

import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.List;

public class BoilerHeating extends ProcessingRecipe<RecipeWrapper> {
    public static final Logger LOGGER = LogUtils.getLogger();

    public BoilerHeating(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(ModRecipeTypes.BOILER_HEATING, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 0;
    }

    @Override
    protected int getMaxOutputCount() {
        return 0;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 1;
    }

    public FluidIngredient getRequiredFluid() {
        if (fluidIngredients.isEmpty())
            throw new IllegalStateException("Heat Exchange Recipe: " + id.toString() + " has no fluid ingredient!");
        return fluidIngredients.get(0);
    }

    public FluidStack getResultingFluid() {
        if (fluidResults.isEmpty())
            throw new IllegalStateException("Heat Exchange: " + id.toString() + " has no fluid output!");
        return fluidResults.get(0);
    }

    public Fluid getFluidForIngredient() {
        return getRequiredFluid().getMatchingFluidStacks().get(0).getFluid();
    }

    public static boolean match(BoilerHeaterBlockEntityOld heater, BoilerHeating recipe) {
        if (recipe.getFluidResults().isEmpty()) return false;

        return apply(heater, recipe, true);
    }

    private static boolean apply(BoilerHeaterBlockEntityOld heater, BoilerHeating recipe, boolean test) {

        IFluidHandler availableFluids = heater.getCapability(ForgeCapabilities.FLUID_HANDLER)
                .orElse(null);

        if (availableFluids == null)
            return false;

        List<FluidIngredient> fluidIngredients = recipe.getFluidIngredients();
        List<FluidStack> recipeOutputFluids = new ArrayList<>();

        int recipeScaler = test ? 5000 : heater.getRecipeScaler();

        boolean fluidsAffected = false;
        for (boolean simulate : Iterate.trueAndFalse) {
            if (!simulate && test) {
                heater.setRecipeScaler(recipeScaler);
                return true;
            }

            int[] extractedFluidsFromTank = new int[availableFluids.getTanks()];

            FluidIngredients:
            for (FluidIngredient fluidIngredient : fluidIngredients) {
                int amountRequired = fluidIngredient.getRequiredAmount();

                for (int tank = 0; tank < availableFluids.getTanks(); tank++) {
                    FluidStack fluidStack = availableFluids.getFluidInTank(tank);
                    if (simulate && fluidStack.getAmount() <= extractedFluidsFromTank[tank])
                        continue;
                    if (!fluidIngredient.test(fluidStack))
                        continue;

                    if (simulate) {
                        recipeScaler = Math.min(Mth.floor((float) fluidStack.getAmount() / amountRequired), recipeScaler);
                        recipeScaler = Math.max(recipeScaler, 1);
                    }

                    int drainedAmount = Math.min(recipeScaler * amountRequired, fluidStack.getAmount());
                    if (!simulate) {
                        fluidStack.shrink(drainedAmount);
                        fluidsAffected = true;
                    }
                    amountRequired = recipeScaler * amountRequired - drainedAmount;
                    if (amountRequired != 0)
                        continue;
                    extractedFluidsFromTank[tank] += drainedAmount;
                    continue FluidIngredients;
                }

                // something wasn't found
                return false;
            }
            if (fluidsAffected) {
                heater.getBehaviour(SmartFluidTankBehaviour.INPUT).forEach(SmartFluidTankBehaviour.TankSegment::onFluidStackChanged);
                heater.getBehaviour(SmartFluidTankBehaviour.OUTPUT).forEach(SmartFluidTankBehaviour.TankSegment::onFluidStackChanged);
            }

            if (simulate) {
                for (FluidStack fluidStack : recipe.getFluidResults()) {
                    if (!fluidStack.isEmpty()) recipeOutputFluids.add(fluidStack);
                }
            }

            if (!heater.acceptOutputs(recipeOutputFluids, simulate))
                return false;
        }

        return true;
    }

    public static boolean apply(BoilerHeaterBlockEntityOld heater, BoilerHeating recipe) {
        return apply(heater, recipe, false);
    }

    @Override
    public boolean matches(RecipeWrapper pContainer, Level pLevel) {
        return false;
    }
}
