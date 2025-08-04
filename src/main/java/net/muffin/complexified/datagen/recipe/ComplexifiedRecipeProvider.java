package net.muffin.complexified.datagen.recipe;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidType;
import net.muffin.complexified.block.ModBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class ComplexifiedRecipeProvider extends RecipeProvider {
    static final List<ProcessingRecipeGen> GENERATORS = new ArrayList<>();
    static final int BUCKET = FluidType.BUCKET_VOLUME;
    static final int BOTTLE = 250;

    public ComplexifiedRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
    }

    public static void registerAllProcessing(DataGenerator gen, PackOutput output) {
        GENERATORS.add(new ComplexifiedBoilerHeatingRecipeGen(output));
        gen.addProvider(true, new DataProvider() {
            @Override
            public CompletableFuture<?> run(CachedOutput dc) {
                return CompletableFuture.allOf(GENERATORS.stream()
                        .map(gen -> gen.run(dc))
                        .toArray(CompletableFuture[]::new)
                );
            }

            @Override
            public String getName() {
                return "Complexified's Processing Recipes";
            }
        });
    }
}