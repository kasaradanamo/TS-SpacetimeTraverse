package net.kasara.ts_spacetime_traverse.fabric.item;

import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.item.SpacetimeEyeItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.function.Function;

public class ModItems {

    // 時空の目アイテムの登録
    public static final Item SPACETIME_EYE = registerItemAndAddToTab("spacetime_eye",
            pros -> new SpacetimeEyeItem(pros.fireResistant().rarity(Rarity.EPIC)));

    private static Item registerItem(String name, Function<Item.Properties, Item> factory) {
        ResourceLocation id = new ResourceLocation(TokorotenSlimeCommon.MOD_ID, name);
        return Registry.register(BuiltInRegistries.ITEM, id, factory.apply(new Item.Properties()));
    }

    private static Item registerItemAndAddToTab(String name, Function<Item.Properties, Item> factory) {
        Item item = registerItem(name, factory);
        TokorotenSlimeAPI.addItemToTab(() -> item);
        return item;
    }

    /**
     * ModItemsの登録処理を呼び出す
     */
    public static void register() {
        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Mod Items for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }
}
