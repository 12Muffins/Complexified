package net.muffin.complexified.block;

import com.simibubi.create.AllTags;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.decoration.bracket.BracketBlock;
import com.simibubi.create.content.decoration.bracket.BracketBlockItem;
import com.simibubi.create.content.decoration.bracket.BracketGenerator;
import com.simibubi.create.content.fluids.tank.FluidTankModel;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.item.ItemDescription;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.muffin.complexified.Complexified;
import net.muffin.complexified.block.custom.*;
import net.muffin.complexified.foundation.block.behaviour.mounted.ComplexifiedMountedStorageTypes;
import net.muffin.complexified.foundation.block.behaviour.mounted.boiler.BoilerHeaterMountedStorageType;
import net.muffin.complexified.foundation.blockEntity.behaviour.fluid.heater.BoilerHeaterGenerator;
import net.muffin.complexified.foundation.blockEntity.behaviour.fluid.heater.BoilerHeaterModel;
import net.muffin.complexified.foundation.blockEntity.behaviour.fluid.heater.HeatingElementGenerator;
import net.muffin.complexified.item.BoilerHeaterBlockItem;
import net.muffin.complexified.item.ComplexifiedCreativeModeTabs;
import net.muffin.complexified.item.HeatingElementBlockItem;
import net.muffin.complexified.tag.ModTags;

import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType.mountedFluidStorage;
import static com.simibubi.create.foundation.data.BlockStateGen.simpleBlock;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;


public class ModBlocks {
    private static final CreateRegistrate REGISTRATE = Complexified.registrate();

    static {
        REGISTRATE.setCreativeTab(ComplexifiedCreativeModeTabs.BLOCKS_CREATIVE_TAB);
    }


    public static final BlockEntry<AndesiteMachineBlock> ANDESITE_MACHINE =
            REGISTRATE.block("andesite_machine", AndesiteMachineBlock::new)
            .initialProperties(SharedProperties::netheriteMetal)
            .properties(p -> p.sound(SoundType.WOOD))
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
            .transform(axeOrPickaxe()).defaultLoot()
            .tag(ModTags.BlockTags.MACHINE.tag)
            .blockstate((c, p) ->
                    p.horizontalBlock(c.get(), AssetLookup.standardModel(c, p), 180))
            .simpleItem()
            .register();

    public static final BlockEntry<SealedMachineBlock> SEALED_MACHINE =
            REGISTRATE.block("sealed_machine", SealedMachineBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.sound(SoundType.WOOD))
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
            .transform(pickaxeOnly()).defaultLoot()
            .tag(ModTags.BlockTags.MACHINE.tag)
            .blockstate((c, p) ->
                    p.horizontalBlock(c.get(), AssetLookup.standardModel(c, p), 180))
            .simpleItem()
            .register();

    public static final BlockEntry<BrassMachineBlock> BRASS_MACHINE =
            REGISTRATE.block("brass_machine", BrassMachineBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.sound(SoundType.WOOD))
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
                    .transform(axeOrPickaxe()).defaultLoot()
                    .tag(ModTags.BlockTags.MACHINE.tag)
                    .blockstate((c, p) ->
                            p.horizontalBlock(c.get(), AssetLookup.standardModel(c, p), 180))
                    .simpleItem()
                    .register();

    public static final BlockEntry<SturdyMachineBlock> STURDY_MACHINE =
            REGISTRATE.block("sturdy_machine", SturdyMachineBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.sound(SoundType.COPPER))
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
                    .transform(pickaxeOnly())
                    .defaultLoot()
                    .tag(ModTags.BlockTags.MACHINE.tag)
                    .blockstate((c, p) ->
                            p.horizontalBlock(c.get(), AssetLookup.standardModel(c, p), 180))
                    .simpleItem()
                    .register();

    public static final BlockEntry<ResinExtractorBlock> RESIN_EXTRACTOR =
            REGISTRATE.block("resin_extractor", ResinExtractorBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.sound(SoundType.COPPER))
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
            .tag(AllTags.AllBlockTags.BRITTLE.tag)
            .transform(pickaxeOnly()).defaultLoot().lang("Arboreal Extractor")
            .blockstate((c, p) ->
                    p.horizontalBlock(c.get(), AssetLookup.standardModel(c, p), 180))
            .simpleItem()
            .register();

    // texture

    public static final BlockEntry<BoilerHeaterBlock> BOILER_HEATER =
            REGISTRATE.block("boiler_heater", BoilerHeaterBlock::new)
                    .initialProperties(SharedProperties::netheriteMetal)
                    .properties(p -> p.mapColor(MapColor.STONE).noOcclusion())
                    .transform(axeOrPickaxe()).defaultLoot()
                    .blockstate(new BoilerHeaterGenerator()::generate)
                    .onRegister(CreateRegistrate.blockModel(() -> BoilerHeaterModel::new))
                    .transform(mountedFluidStorage(ComplexifiedMountedStorageTypes.BOILER_HEATER))
                    .onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.c_complexified.heater"))
                    .addLayer(() -> RenderType::cutoutMipped)
                    .item().model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/boiler_heater/block")))
                    .build()
                    .register();

    public static final BlockEntry<HeatingElementBlock> COPPER_HEATING_ELEMENT = REGISTRATE.block("copper_heating_element", HeatingElementBlock::new)
            .blockstate(new HeatingElementGenerator("copper")::generate)
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .transform(pickaxeOnly())
            .item(HeatingElementBlockItem::new)
            .tag(ModTags.ItemTags.HEATING_ELEMENT.tag)
            .transform(HeatingElementGenerator.itemModel("copper"))
            .register();

    // TODO: add custom renderer. See: PipeAttachmentModel.putBracket(BlockState state)
    // TODO: add removeElement() in BoilerHeaterBlock

    public static void register() {
    }
}