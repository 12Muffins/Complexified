package net.muffin.complexified.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.muffin.complexified.Complexified;
import net.muffin.complexified.datagen.recipe.ComplexifiedRecipeProvider;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static net.muffin.complexified.Complexified.MOD_ID;

public class ComplexifiedDataGenerators {
    public static void gatherDataHighPriority(GatherDataEvent event) {
        if (event.getModContainer().getModId().contains(MOD_ID)) addExtraRegistrateData();
    }

    public static void gatherData(GatherDataEvent event) {
        if (!event.getModContainer().getModId().contains(MOD_ID)) return;

        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        ComplexifiedRecipeProvider.registerAllProcessing(generator, output);
    }

    private static void addExtraRegistrateData() {
        DataGenTags.addGenerators();

        Complexified.registrate().addDataGenerator(ProviderType.LANG, provider ->{
            BiConsumer<String, String> langConsumer = provider::add;

            provideDefaultLang("tooltips", langConsumer);
            provideDefaultLang("goggle_info", langConsumer);
        });
    }

    private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
        String path = "assets/c_complexified/lang/default/" + fileName + ".json";
        JsonElement jsonElement = FilesHelper.loadJsonResource(path);
        if (jsonElement == null) {
            throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getAsString();
            consumer.accept(key, value);
        }
    }
}
