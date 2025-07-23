package net.muffin.complexified.block.entity;

import com.simibubi.create.content.redstone.displayLink.LinkBulbRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.muffin.complexified.Complexified;
import net.muffin.complexified.block.ModBlocks;
import net.muffin.complexified.block.renderer.BoilerHeaterRenderer;
import net.muffin.complexified.block.visual.BoilerHeaterVisual;

public class ModBlockEntities {
    public static final BlockEntityEntry<SturdyMachineBlockEntity> STURDY_MACHINE = Complexified.REGISTRATE
            .blockEntity("sturdy_machine", SturdyMachineBlockEntity::new)
            .validBlocks(ModBlocks.STURDY_MACHINE)
            .renderer(() -> LinkBulbRenderer::new)
            .register();

    public static final BlockEntityEntry<BoilerHeaterBlockEntityOld> BOILER_HEATER_OLD = Complexified.REGISTRATE
            .blockEntity("industrial_heater", BoilerHeaterBlockEntityOld::new)
            .validBlocks(ModBlocks.BOILER_HEATER_OLD, ModBlocks.BOILER_HEATER_STRUCTURAL)
//            .renderer(() -> BoilerHeaterRenderer::new)
            .register();

    public static final BlockEntityEntry<BoilerHeaterBlockEntity> BOILER_HEATER = Complexified.REGISTRATE
            .blockEntity("boiler_heater", BoilerHeaterBlockEntity::new)
            .visual(() -> BoilerHeaterVisual::new)
            .validBlocks(ModBlocks.BOILER_HEATER)
            .renderer(() -> BoilerHeaterRenderer::new)
            .register();

    public static void register() {}
}
