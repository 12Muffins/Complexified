package net.muffin.complexified.foundation.mixin;

import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.ForgeMod;
import net.muffin.complexified.fluid.ModFluids;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class PahoehoeEntityDamageMixin {

    private Entity self() {
        return (Entity) (Object) this;
    }

    @Shadow
    protected boolean firstTick;

    /**
     * @author 12Muffins
     * @reason Copy behaviour of lava to Pahoehoe
     */
    @Inject(method = "isInLava", at = @At("RETURN"), cancellable = true)
    public void isInLava(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;

        double height = self().getFluidTypeHeight(ModFluids.PAHOEHOE.getType());
        boolean newCheck = !firstTick && height > 2/8D;
        cir.setReturnValue(newCheck);
    }
}
