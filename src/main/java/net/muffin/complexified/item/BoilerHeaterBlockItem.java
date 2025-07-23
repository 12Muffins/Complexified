package net.muffin.complexified.item;

import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

public class BoilerHeaterBlockItem extends BlockItem {
    public BoilerHeaterBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    public @NotNull InteractionResult place(BlockPlaceContext ctx) {
        InteractionResult result = super.place(ctx);
        if (result != InteractionResult.FAIL)
            return result;
        if (ctx.getLevel().isClientSide()) DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> showBounds(ctx));
        return result;
    }

    @OnlyIn(Dist.CLIENT)
    public void showBounds(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Vec3 contract = Vec3.atLowerCornerOf(Direction.get(Direction.AxisDirection.POSITIVE, Direction.Axis.Y)
                .getNormal());
        if (!(context.getPlayer()instanceof LocalPlayer localPlayer))
            return;
        Outliner.getInstance().showAABB(Pair.of("boiler_heater", pos), new AABB(pos).inflate(1)
                        .deflate(contract.x, contract.y, contract.z))
                .colored(0xFF_ff5d6c);
        CreateLang.translate("large_water_wheel.not_enough_space")
                .color(0xFF_ff5d6c)
                .sendStatus(localPlayer);
    }

}
