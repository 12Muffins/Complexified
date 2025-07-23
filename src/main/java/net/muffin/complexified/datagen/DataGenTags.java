package net.muffin.complexified.datagen;


import com.mojang.logging.LogUtils;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.muffin.complexified.fluid.ModFluids;
import net.muffin.complexified.tag.ModTags;
import org.slf4j.Logger;

import static net.muffin.complexified.Complexified.REGISTRATE;

@SuppressWarnings("deprecation")
public class DataGenTags {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static void addGenerators() {
        LOGGER.info("Applying tags");
        REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, DataGenTags::genBlockTags);
        REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, DataGenTags::genItemTags);
        REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, DataGenTags::genFluidTags);
    }
    private static void genItemTags(RegistrateTagsProvider<Item> provIn) {
        var prov = new TagGen.CreateTagsProvider<>(provIn, Item::builtInRegistryHolder);

        prov.tag(ModTags.ItemTags.MECHANISM.tag)
                .add(AllItems.PRECISION_MECHANISM.get())
        ;

        prov.tag(ModTags.ItemTags.INCOMPLETE_MECHANISM.tag)
                .add(AllItems.INCOMPLETE_PRECISION_MECHANISM.get())
        ;

        prov.tag(AllTags.AllItemTags.UPRIGHT_ON_BELT.tag)
                .add(Items.POWDER_SNOW_BUCKET)
                .add(Items.DRAGON_BREATH)
                .add(Items.EXPERIENCE_BOTTLE)
                .add(Items.LIGHT)
                .addTag(ItemTags.CANDLES)
        ;

        LOGGER.info("Item tags applied");

        for (var tag : ModTags.ItemTags.values()) if (tag.alwaysDatagen) prov.getOrCreateRawBuilder(tag.tag);
    }

    private static void genBlockTags(RegistrateTagsProvider<Block> provIn) {
        TagGen.CreateTagsProvider<Block> prov = new TagGen.CreateTagsProvider<>(provIn, Block::builtInRegistryHolder);
        prov.tag(ModTags.BlockTags.MACHINE.tag).addTag(BlockTags.SAND);

/// Example:
/// prov.tag(ModTags.BlockTags.INDUSTRIAL_FAN_HEATER.tag)
///         .add(Blocks.LAVA)
///         .add(DesiresBlocks.SEETHING_SAIL.get())
///         .add(AllBlocks.BLAZE_BURNER.get());

//        prov.tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
//                .addTag(ModTags.BlockTags.MACHINE.tag);

        LOGGER.info("Block tags applied");

        for (var tag : ModTags.BlockTags.values()) if (tag.alwaysDatagen) prov.getOrCreateRawBuilder(tag.tag);
    }

    private static void genFluidTags(RegistrateTagsProvider<Fluid> provIn) {
        TagGen.CreateTagsProvider<Fluid> prov = new TagGen.CreateTagsProvider<>(provIn, Fluid::builtInRegistryHolder);

        prov.tag(FluidTags.LAVA)
                .add(ModFluids.PAHOEHOE.getSource())
                .add(ModFluids.PAHOEHOE.get())
        ;
    }
}
