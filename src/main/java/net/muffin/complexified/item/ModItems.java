package net.muffin.complexified.item;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.muffin.complexified.Complexified;
import net.muffin.complexified.tag.ModTags;

public class ModItems {
    private static final CreateRegistrate REGISTRATE = Complexified.registrate();

    static {
        REGISTRATE.setCreativeTab(ComplexifiedCreativeModeTabs.ITEMS_CREATIVE_TAB);
    }

    public static final ItemEntry<Item>
    KINETIC_MECHANISM = taggedIngredient("kinetic_mechanism", ModTags.ItemTags.KINETIC_MECHANISM.tag, ModTags.ItemTags.MECHANISM.tag),
    SEALED_MECHANISM = taggedIngredient("sealed_mechanism", ModTags.ItemTags.MECHANISM.tag),
    STURDY_MECHANISM = REGISTRATE.item("sturdy_mechanism", Item::new).properties(Item.Properties::fireResistant).tag(ModTags.ItemTags.MECHANISM.tag).register();

    public static final ItemEntry<SequencedAssemblyItem>
    INCOMPLETE_KINETIC_MECHANISM = sequencedIngredient("incomplete_kinetic_mechanism", ModTags.ItemTags.INCOMPLETE_MECHANISM.tag),
    INCOMPLETE_SEALED_MECHANISM = sequencedIngredient("incomplete_sealed_mechanism", ModTags.ItemTags.INCOMPLETE_MECHANISM.tag),
    INCOMPLETE_STURDY_MECHANISM = sequencedIngredient("incomplete_sturdy_mechanism", ModTags.ItemTags.INCOMPLETE_MECHANISM.tag);


    @SafeVarargs
    private static ItemEntry<Item> taggedIngredient(String name, TagKey<Item>... tags) {
        return REGISTRATE.item(name, Item::new)
                .tag(tags)
                .register();
    }

    private static ItemEntry<SequencedAssemblyItem> sequencedIngredient(String name) {
        return REGISTRATE.item(name, SequencedAssemblyItem::new)
                .register();
    }

    private static ItemEntry<SequencedAssemblyItem> sequencedIngredient(String name, TagKey<Item>... tags) {
        return REGISTRATE.item(name, SequencedAssemblyItem::new).tag(tags)
                .register();
    }

    private static ItemEntry<Item> ingredient(String name) {
        return REGISTRATE.item(name, Item::new)
                .register();
    }

    public static void register() {
    }

}
