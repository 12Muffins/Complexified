package net.muffin.complexified.foundation.mixin;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.spongepowered.asm.service.MixinService;

import java.util.List;

public class ConditionalMixinManager {

//    public static boolean shouldApply(String className) {
//        try {
//            List<AnnotationNode> annotationNodes = MixinService.getService().getBytecodeProvider().getClassNode(className).visibleAnnotations;
//            if (annotationNodes == null) return true;
//
//            boolean shouldApply = true;
//            for (AnnotationNode node : annotationNodes) {
//                if (node.desc.equals(Type.getDescriptor(ConditionalMixin.class))) {
//                    List<Mods> mods = Annotations.getValue(node, "mods", true, Mods.class);
//                    boolean applyIfPresent = Annotations.getValue(node, "applyIfPresent", Boolean.TRUE);
//                    boolean anyModsLoaded = anyModsLoaded(mods);
//                    shouldApply = anyModsLoaded == applyIfPresent;
//                    CRMixinPlugin.LOGGER.debug("{} is{}being applied because the mod(s) {} are{}loaded", className, shouldApply ? " " : " not ", mods, anyModsLoaded ? " " : " not ");
//                }
//                if (node.desc.equals(Type.getDescriptor(DevEnvMixin.class))) {
//                    shouldApply &= Utils.isDevEnv();
//                }
//            }
//            return shouldApply;
//        } catch (ClassNotFoundException | IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static boolean anyModsLoaded(List<Mods> mods) {
//        for (Mods mod : mods) {
//            if (mod.isLoaded) return true;
//        }
//        return false;
//    }
}
