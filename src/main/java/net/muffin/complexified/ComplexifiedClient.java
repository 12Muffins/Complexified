package net.muffin.complexified;

import com.simibubi.create.AllPartialModels;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.muffin.complexified.block.renderer.ComplexifiedPartialModels;

public class ComplexifiedClient {
    public static void onCtorClient(IEventBus modEventBus) {
        modEventBus.addListener(ComplexifiedClient::clientInit);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        ComplexifiedPartialModels.init();
    }
}