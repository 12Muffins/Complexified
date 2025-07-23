package net.muffin.complexified.item;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.TagDependentIngredientItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.muffin.complexified.Complexified;
import net.muffin.complexified.block.ModBlocks;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;


import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ComplexifiedCreativeModeTabs {
    private static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Complexified.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ITEMS_CREATIVE_TAB = REGISTER.register("items",
            () -> CreativeModeTab.builder()
                    .title(Component.translatableWithFallback("itemGroup."+ Complexified.MOD_ID +".items", Complexified.NAME + "'s Items"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(ModItems.KINETIC_MECHANISM::asStack)
                    .displayItems(new ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator(true, ComplexifiedCreativeModeTabs.ITEMS_CREATIVE_TAB))
                    .build());

    public static final RegistryObject<CreativeModeTab> BLOCKS_CREATIVE_TAB = REGISTER.register("machines",
            () -> CreativeModeTab.builder()
                    .title(Component.translatableWithFallback("itemGroup."+ Complexified.MOD_ID +".machines", Complexified.NAME + "'s Machines"))
                    .withTabsBefore(ITEMS_CREATIVE_TAB.getKey())
                    .icon(ModBlocks.ANDESITE_MACHINE::asStack)
                    .displayItems(new ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator(false, ComplexifiedCreativeModeTabs.BLOCKS_CREATIVE_TAB))
                    .build());

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }

    private static class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {
        private static final Predicate<Item> IS_ITEM_3D_PREDICATE;

        static {
            MutableObject<Predicate<Item>> isItem3d = new MutableObject<>(item -> false);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                isItem3d.setValue(item -> {
                    ItemRenderer itemRenderer = Minecraft.getInstance()
                            .getItemRenderer();
                    BakedModel model = itemRenderer.getModel(new ItemStack(item), null, null, 0);
                    return model.isGui3d();
                });
            });
            IS_ITEM_3D_PREDICATE = isItem3d.getValue();
        }

        private final boolean addItems;
        private final RegistryObject<CreativeModeTab> tabFilter;

        public RegistrateDisplayItemsGenerator(boolean addItems, RegistryObject<CreativeModeTab> tabFilter) {
            this.addItems = addItems;
            this.tabFilter = tabFilter;
        }

        private static Predicate<Item> makeExclusionPredicate() {
            Set<Item> exclusions = new ReferenceOpenHashSet<>();

            List<ItemProviderEntry<?>> simpleExclusions = List.of(
                    ModItems.INCOMPLETE_KINETIC_MECHANISM,
                    ModItems.INCOMPLETE_SEALED_MECHANISM,
                    ModItems.INCOMPLETE_STURDY_MECHANISM
            );

            List<ItemEntry<TagDependentIngredientItem>> tagDependentExclusions = List.of(
                    // TO ADD
            );

            for (ItemProviderEntry<?> entry : simpleExclusions) {
                exclusions.add(entry.asItem());
            }

//            for (ItemEntry<TagDependentIngredientItem> entry : tagDependentExclusions) {
//                TagDependentIngredientItem item = entry.get();
//                if (item.shouldHide()) {
//                    exclusions.add(entry.asItem());
//                }
//            }

            return exclusions::contains;
        }

        private static List<ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering> makeOrderings() {
            List<ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering> orderings = new ReferenceArrayList<>();

            Map<ItemProviderEntry<?>, ItemProviderEntry<?>> simpleBeforeOrderings = Map.of(
                    // TO ADD
            );

            Map<ItemProviderEntry<?>, ItemProviderEntry<?>> simpleAfterOrderings = Map.of(
                    // TO ADD
            );

            simpleBeforeOrderings.forEach((entry, otherEntry) -> {
                orderings.add(ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering.before(entry.asItem(), otherEntry.asItem()));
            });

            simpleAfterOrderings.forEach((entry, otherEntry) -> {
                orderings.add(ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering.after(entry.asItem(), otherEntry.asItem()));
            });

            return orderings;
        }

        private static Function<Item, ItemStack> makeStackFunc() {
            Map<Item, Function<Item, ItemStack>> factories = new Reference2ReferenceOpenHashMap<>();

//            Map<ItemProviderEntry<?>, Function<Item, ItemStack>> simpleFactories = Map.of(
//                    AllItems.COPPER_BACKTANK, item -> {
//                        ItemStack stack = new ItemStack(item);
//                        stack.getOrCreateTag().putInt("Air", BacktankUtil.maxAirWithoutEnchants());
//                        return stack;
//                    },
//                    AllItems.NETHERITE_BACKTANK, item -> {
//                        ItemStack stack = new ItemStack(item);
//                        stack.getOrCreateTag().putInt("Air", BacktankUtil.maxAirWithoutEnchants());
//                        return stack;
//                    }
//            );
//
//            simpleFactories.forEach((entry, factory) -> {
//                factories.put(entry.asItem(), factory);
//            });

            return item -> {
                Function<Item, ItemStack> factory = factories.get(item);
                if (factory != null) {
                    return factory.apply(item);
                }
                return new ItemStack(item);
            };
        }

        private static Function<Item, CreativeModeTab.TabVisibility> makeVisibilityFunc() {
            Map<Item, CreativeModeTab.TabVisibility> visibilities = new Reference2ObjectOpenHashMap<>();

            Map<ItemProviderEntry<?>, CreativeModeTab.TabVisibility> simpleVisibilities = Map.of(
                    // TO ADD
            );

            simpleVisibilities.forEach((entry, factory) -> {
                visibilities.put(entry.asItem(), factory);
            });

            return item -> {
                CreativeModeTab.TabVisibility visibility = visibilities.get(item);
                if (visibility != null) {
                    return visibility;
                }
                return CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
            };
        }

        @Override
        public void accept(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
            Predicate<Item> exclusionPredicate = makeExclusionPredicate();
            List<ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering> orderings = makeOrderings();
            Function<Item, ItemStack> stackFunc = makeStackFunc();
            Function<Item, CreativeModeTab.TabVisibility> visibilityFunc = makeVisibilityFunc();

            List<Item> items = new LinkedList<>();
            if (addItems) {
                items.addAll(collectItems(exclusionPredicate.or(IS_ITEM_3D_PREDICATE.negate())));
            }
            items.addAll(collectBlocks(exclusionPredicate));
            if (addItems) {
                items.addAll(collectItems(exclusionPredicate.or(IS_ITEM_3D_PREDICATE)));
            }

            applyOrderings(items, orderings);
            outputAll(output, items, stackFunc, visibilityFunc);
        }

        private List<Item> collectBlocks(Predicate<Item> exclusionPredicate) {
            List<Item> items = new ReferenceArrayList<>();
            for (RegistryEntry<Block> entry : Complexified.registrate().getAll(Registries.BLOCK)) {
                if (!CreateRegistrate.isInCreativeTab(entry, tabFilter))
                    continue;
                Item item = entry.get()
                        .asItem();
                if (item == Items.AIR)
                    continue;
                if (!exclusionPredicate.test(item))
                    items.add(item);
            }
            items = new ReferenceArrayList<>(new ReferenceLinkedOpenHashSet<>(items));
            return items;
        }

        private List<Item> collectItems(Predicate<Item> exclusionPredicate) {
            List<Item> items = new ReferenceArrayList<>();
            for (RegistryEntry<Item> entry : Complexified.registrate().getAll(Registries.ITEM)) {
                if (!CreateRegistrate.isInCreativeTab(entry, tabFilter))
                    continue;
                Item item = entry.get();
                if (item instanceof BlockItem)
                    continue;
                if (!exclusionPredicate.test(item))
                    items.add(item);
            }
            return items;
        }

        private static void applyOrderings(List<Item> items, List<ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering> orderings) {
            for (ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering ordering : orderings) {
                int anchorIndex = items.indexOf(ordering.anchor());
                if (anchorIndex != -1) {
                    Item item = ordering.item();
                    int itemIndex = items.indexOf(item);
                    if (itemIndex != -1) {
                        items.remove(itemIndex);
                        if (itemIndex < anchorIndex) {
                            anchorIndex--;
                        }
                    }
                    if (ordering.type() == ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering.Type.AFTER) {
                        items.add(anchorIndex + 1, item);
                    } else {
                        items.add(anchorIndex, item);
                    }
                }
            }
        }

        private static void outputAll(CreativeModeTab.Output output, List<Item> items, Function<Item, ItemStack> stackFunc, Function<Item, CreativeModeTab.TabVisibility> visibilityFunc) {
            for (Item item : items) {
                output.accept(stackFunc.apply(item), visibilityFunc.apply(item));
            }
        }

        private record ItemOrdering(Item item, Item anchor, ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering.Type type) {
            public static ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering before(Item item, Item anchor) {
                return new ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering(item, anchor, ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering.Type.BEFORE);
            }

            public static ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering after(Item item, Item anchor) {
                return new ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering(item, anchor, ComplexifiedCreativeModeTabs.RegistrateDisplayItemsGenerator.ItemOrdering.Type.AFTER);
            }

            public enum Type {
                BEFORE,
                AFTER;
            }
        }
    }
}
