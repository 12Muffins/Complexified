package net.muffin.complexified;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.muffin.complexified.block.ModBlocks;
import net.muffin.complexified.block.custom.BoilerHeaterBlock;
import net.muffin.complexified.block.entity.ModBlockEntities;
import net.muffin.complexified.datagen.ComplexifiedDataGenerators;
import net.muffin.complexified.fluid.ModFluids;
import net.muffin.complexified.foundation.block.BoilerHeaters;
import net.muffin.complexified.foundation.block.behaviour.mounted.ComplexifiedMountedStorageTypes;
import net.muffin.complexified.foundation.block.behaviour.movement.ComplexifiedMovementChecks;
import net.muffin.complexified.item.ComplexifiedCreativeModeTabs;
import net.muffin.complexified.item.ModItems;
import net.muffin.complexified.recipe.ModRecipeTypes;
import net.muffin.complexified.tag.ModTags;

import static com.simibubi.create.impl.contraption.BlockMovementChecksImpl.registerAttachedCheck;

@Mod(Complexified.MOD_ID)
public class Complexified {

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public static final String MOD_ID = "c_complexified";
    public static final String NAME = "Complexified";
    // private static final Logger LOGGER = LogUtils.getLogger();
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(Complexified.MOD_ID)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            /*.andThen(TooltipModifier.mapNull())*/);

    public Complexified(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        REGISTRATE.registerEventListeners(modEventBus);

        ModTags.init();
        ComplexifiedCreativeModeTabs.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModBlocks.register();
        ModItems.register();
        ModFluids.register();
        ModBlockEntities.register();
        ComplexifiedMountedStorageTypes.register();
        ComplexifiedMovementChecks.register();

//        ModCreativeModeTabs.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(EventPriority.HIGHEST, ComplexifiedDataGenerators::gatherDataHighPriority);
        modEventBus.addListener(EventPriority.LOWEST, ComplexifiedDataGenerators::gatherData);
        modEventBus.addListener(Complexified::init);

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ComplexifiedClient.onCtorClient(modEventBus));
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {}

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    // You can use EventBusSubscriber to automatically register all static methods
    // in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }

    public static void init(final FMLCommonSetupEvent event) {
        ModFluids.registerFluidInteractions();
        event.enqueueWork(() -> {
            BoilerHeaters.registerDefaults();
        });
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }


    public static CreateRegistrate registrate() {
        if (!STACK_WALKER.getCallerClass().getPackageName().startsWith("net.muffin.complexified"))
            throw new UnsupportedOperationException("Other mods are not permitted to use complexified's registrate instance.");
        return REGISTRATE;
    }

}
