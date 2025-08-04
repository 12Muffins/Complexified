package net.muffin.complexified.foundation.mixin;

import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.simibubi.create.content.contraptions.StructureTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.muffin.complexified.block.entity.BoilerHeaterBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Mixin(Contraption.class)
public abstract class HeaterElementContraptionMixin {

    @Shadow(remap = false) protected Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks;
    @Shadow(remap = false) protected Multimap<BlockPos, StructureTemplate.StructureBlockInfo> capturedMultiblocks;
    @Shadow(remap = false) protected ContraptionWorld world;


    //    @Inject(method = "translateMultiblockControllers", slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;keySet()Ljava/util/Set;")),at = @At(
//                    value = "INVOKE", target = "Ljava/lang/Iterable;forEach(Ljava/util/function/Consumer;)V"))
    @Inject(method = "translateMultiblockControllers", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;keySet()Ljava/util/Set;", ordinal = 0), remap = false, cancellable = true)
    private void onTranslateMultiblockControllers(StructureTransform transform, CallbackInfo ci) {

        capturedMultiblocks.keySet().forEach(controllerPos -> {
            Collection<StructureTemplate.StructureBlockInfo> multiblockParts = capturedMultiblocks.get(controllerPos);
            Optional<BoundingBox> optionalBoundingBox = BoundingBox.encapsulatingPositions(multiblockParts.stream().map(info -> transform.apply(info.pos())).toList());
            if (optionalBoundingBox.isEmpty())
                return;

            BoundingBox boundingBox = optionalBoundingBox.get();
            BlockPos newControllerPos = new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
            BlockPos otherPos = transform.unapply(newControllerPos);

            multiblockParts.forEach(info -> info.nbt().put("Controller", NbtUtils.writeBlockPos(newControllerPos)));

            if (controllerPos.equals(otherPos))
                return;

            // swap nbt data to the new controller position
            StructureTemplate.StructureBlockInfo prevControllerInfo = blocks.get(controllerPos);
            StructureTemplate.StructureBlockInfo newControllerInfo = blocks.get(otherPos);
            if (prevControllerInfo == null || newControllerInfo == null)
                return;

            blocks.put(otherPos, new StructureTemplate.StructureBlockInfo(newControllerInfo.pos(), newControllerInfo.state(), prevControllerInfo.nbt()));
            blocks.put(controllerPos, new StructureTemplate.StructureBlockInfo(prevControllerInfo.pos(), prevControllerInfo.state(), newControllerInfo.nbt()));
        });

        ci.cancel();
    }


//    /**
//     * @author 12Muffins
//     * @reason preserve NBT on the correct block
//     */
//    @Overwrite
//    public void translateMultiblockControllers(StructureTransform transform) {
//        if (transform.rotationAxis != null && transform.rotationAxis != Direction.Axis.Y && transform.rotation != Rotation.NONE) {
//            capturedMultiblocks.values().forEach(info -> {
//                info.nbt().put("LastKnownPos", NbtUtils.writeBlockPos(BlockPos.ZERO.below(Integer.MAX_VALUE - 1)));
//            });
//            return;
//        }
//
//        capturedMultiblocks.keySet().forEach(controllerPos -> {
//            Collection<StructureTemplate.StructureBlockInfo> multiblockParts = capturedMultiblocks.get(controllerPos);
//            Optional<BoundingBox> optionalBoundingBox = BoundingBox.encapsulatingPositions(multiblockParts.stream().map(info -> transform.apply(info.pos())).toList());
//            if (optionalBoundingBox.isEmpty())
//                return;
//
//            BoundingBox boundingBox = optionalBoundingBox.get();
//            BlockPos newControllerPos = new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
//            BlockPos otherPos = transform.unapply(newControllerPos);
//
//            multiblockParts.forEach(info -> info.nbt().put("Controller", NbtUtils.writeBlockPos(newControllerPos)));
//
//            if (controllerPos.equals(otherPos))
//                return;
//
//            // swap nbt data to the new controller position
//            StructureTemplate.StructureBlockInfo prevControllerInfo = blocks.get(controllerPos);
//            StructureTemplate.StructureBlockInfo newControllerInfo = blocks.get(otherPos);
//            if (prevControllerInfo == null || newControllerInfo == null)
//                return;
//
//            blocks.put(otherPos, new StructureTemplate.StructureBlockInfo(newControllerInfo.pos(), newControllerInfo.state(), prevControllerInfo.nbt()));
//            blocks.put(controllerPos, new StructureTemplate.StructureBlockInfo(prevControllerInfo.pos(), prevControllerInfo.state(), newControllerInfo.nbt()));
//        });
//    }
}
