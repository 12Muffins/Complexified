package net.muffin.complexified.item;

import com.simibubi.create.AllItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.muffin.complexified.Complexified;
import net.minecraft.network.chat.Component;
import net.muffin.complexified.block.ModBlocks;
import net.muffin.complexified.fluid.ModFluids;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, Complexified.MOD_ID);

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

    public static final RegistryObject<CreativeModeTab> ITEM_TABS = CREATIVE_MODE_TABS.register("complexified_items",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.KINETIC_MECHANISM.get()))
                    .displayItems((pParameters, pOutput) -> {
                        if (ModFluids.RESIN.getBlock().isPresent()) pOutput.accept(ModFluids.RESIN.getBlock().get());
                        pOutput.accept(ModItems.KINETIC_MECHANISM.get());
                        pOutput.accept(ModItems.SEALED_MECHANISM.get());
                        pOutput.accept(AllItems.PRECISION_MECHANISM.get());
                        pOutput.accept(ModItems.STURDY_MECHANISM.get());
                    })
                    .title(Component.translatable("creativetab.complexified_items"))
                    .build());

    public static final RegistryObject<CreativeModeTab> BLOCK_TABS = CREATIVE_MODE_TABS.register("complexified_blocks",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.ANDESITE_MACHINE.get()))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModBlocks.ANDESITE_MACHINE.get());
                        pOutput.accept(ModBlocks.BRASS_MACHINE.get());
                        pOutput.accept(ModBlocks.SEALED_MACHINE.get());
                        pOutput.accept(ModBlocks.STURDY_MACHINE.get());
                        pOutput.accept(ModBlocks.RESIN_EXTRACTOR.get());
                    })
                    .title(Component.translatable("creativetab.complexified_blocks"))
                    .build());
}
