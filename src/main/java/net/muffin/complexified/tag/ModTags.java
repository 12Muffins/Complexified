package net.muffin.complexified.tag;

import com.simibubi.create.AllTags;
import net.createmod.catnip.lang.Lang;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.muffin.complexified.Complexified;

import java.util.Collections;

import static com.simibubi.create.AllTags.NameSpace.FORGE;
import static com.simibubi.create.AllTags.NameSpace.MOD;

public class ModTags {
    public static <T> TagKey<T> optionalTag(IForgeRegistry<T> registry,
                                            ResourceLocation id) {
        return registry.tags()
                .createOptionalTagKey(id, Collections.emptySet());
    }

    public static <T> TagKey<T> forgeTag(IForgeRegistry<T> registry, String path) {
        return optionalTag(registry, new ResourceLocation("forge", path));
    }

    public static TagKey<Block> forgeBlockTag(String path) {
        return forgeTag(ForgeRegistries.BLOCKS, path);
    }

    public static TagKey<Item> forgeItemTag(String path) {
        return forgeTag(ForgeRegistries.ITEMS, path);
    }

    public static TagKey<Fluid> forgeFluidTag(String path) {
        return forgeTag(ForgeRegistries.FLUIDS, path);
    }

    public enum NameSpace {

        MOD(Complexified.MOD_ID, false, true),
        //        TIC("tconstruct"),
//        QUARK("quark"),
//        GS("galosphere"),
//        CURIOS("curios"),
        CREATE("create"),
        FORGE("forge");

        public final String id;
        public final boolean optionalDefault;
        public final boolean alwaysDatagenDefault;

        NameSpace(String id) {
            this(id, true, false);
        }

        NameSpace(String id, boolean optionalDefault, boolean alwaysDatagenDefault) {
            this.id = id;
            this.optionalDefault = optionalDefault;
            this.alwaysDatagenDefault = alwaysDatagenDefault;
        }
    }

    public enum BlockTags {
        KINETIC_MACHINE,
        MACHINE
        ;

        public final TagKey<Block> tag;
        public final boolean alwaysDatagen;

        BlockTags() {
            this(NameSpace.MOD);
        }

        BlockTags(ModTags.NameSpace namespace) {
            this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        BlockTags(ModTags.NameSpace namespace, String path) {
            this(namespace, path, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        BlockTags(ModTags.NameSpace namespace, boolean optional, boolean alwaysDatagen) {
            this(namespace, null, optional, alwaysDatagen);
        }

        BlockTags(ModTags.NameSpace namespace, String path, boolean optional, boolean alwaysDatagen) {
            ResourceLocation id = new ResourceLocation(namespace.id, path == null ? Lang.asId(name()) : path);
            if (optional) {
                tag = optionalTag(ForgeRegistries.BLOCKS, id);
            } else {
                tag = net.minecraft.tags.BlockTags.create(id);
            }
            this.alwaysDatagen = alwaysDatagen;
        }

        @SuppressWarnings("deprecation")
        public boolean matches(Block block) {
            return block.builtInRegistryHolder()
                    .is(tag);
        }

        public boolean matches(ItemStack stack) {
            return stack != null && stack.getItem() instanceof BlockItem blockItem && matches(blockItem.getBlock());
        }

        public boolean matches(BlockState state) {
            return state.is(tag);
        }

        private static void init() {
        }
    }

    public enum ItemTags {
        MECHANISM,
        INCOMPLETE_MECHANISM,
        KINETIC_MECHANISM
        ;

        public final TagKey<Item> tag;
        public final boolean alwaysDatagen;

        ItemTags() {
            this(NameSpace.MOD);
        }

        ItemTags(ModTags.NameSpace namespace) {
            this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        ItemTags(ModTags.NameSpace namespace, String path) {
            this(namespace, path, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        ItemTags(ModTags.NameSpace namespace, boolean optional, boolean alwaysDatagen) {
            this(namespace, null, optional, alwaysDatagen);
        }

        ItemTags(ModTags.NameSpace namespace, String path, boolean optional, boolean alwaysDatagen) {
            ResourceLocation id = new ResourceLocation(namespace.id, path == null ? Lang.asId(name()) : path);
            if (optional) {
                tag = optionalTag(ForgeRegistries.ITEMS, id);
            } else {
                tag = net.minecraft.tags.ItemTags.create(id);
            }
            this.alwaysDatagen = alwaysDatagen;
        }

        @SuppressWarnings("deprecation")
        public boolean matches(Item item) {
            return item.builtInRegistryHolder()
                    .is(tag);
        }

        public boolean matches(ItemStack stack) {
            return stack.is(tag);
        }

        private static void init() {
        }
    }

    public enum FluidTags {

        RESIN(FORGE);

        public final TagKey<Fluid> tag;
        public final boolean alwaysDatagen;

        FluidTags() {
            this(MOD);
        }

        FluidTags(AllTags.NameSpace namespace) {
            this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        FluidTags(AllTags.NameSpace namespace, String path) {
            this(namespace, path, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        FluidTags(AllTags.NameSpace namespace, boolean optional, boolean alwaysDatagen) {
            this(namespace, null, optional, alwaysDatagen);
        }

        FluidTags(AllTags.NameSpace namespace, String path, boolean optional, boolean alwaysDatagen) {
            ResourceLocation id = new ResourceLocation(namespace.id, path == null ? Lang.asId(name()) : path);
            if (optional) {
                tag = optionalTag(ForgeRegistries.FLUIDS, id);
            } else {
                tag = net.minecraft.tags.FluidTags.create(id);
            }
            this.alwaysDatagen = alwaysDatagen;
        }

        @SuppressWarnings("deprecation")
        public boolean matches(Fluid fluid) {
            return fluid.is(tag);
        }

        public boolean matches(FluidState state) {
            return state.is(tag);
        }

        private static void init() {
        }

    }



    public static void init() {
        FluidTags.init();
        BlockTags.init();
        ItemTags.init();
    }

}
