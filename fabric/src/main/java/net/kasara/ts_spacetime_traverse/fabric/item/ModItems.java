package net.kasara.ts_spacetime_traverse.fabric.item;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.fabric.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.item.SpacetimeEyeItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.function.Function;

public class ModItems {

    // 時空の目アイテムの登録
    public static final Item SPACETIME_EYE = registerItemAndAddToTab("spacetime_eye", pros -> new SpacetimeEyeItem(
            pros.fireResistant().rarity(Rarity.EPIC)));

    private static Item registerItemAndAddToTab(String name, Function<Item.Properties, Item> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TokorotenSlimeAPI.getModId(), name);
        Item item = Registry.register(
                BuiltInRegistries.ITEM,
                id,
                factory.apply(new Item.Properties())
        );
        // API経由でグループに追加
        TokorotenSlimeAPI.addItemToTab(() -> item);
        return item;
    }

    /**
     * 登録確認用のログ出力
     */
    public static void register() {
        TSSpacetimeTraverse.LOGGER.info("Registering addon Items for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
