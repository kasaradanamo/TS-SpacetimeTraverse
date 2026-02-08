package net.kasara.ts_spacetime_traverse.item;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.function.Function;

/**
 * Modで追加するアイテムを登録するクラス
 */
public class ModItems {

    /** 時空の目アイテムの登録 */
    public static final Item SPACETIME_EYE = registerAndAddToTab("spacetime_eye", setting -> new SpacetimeEyeItem(
            setting.fireproof().rarity(Rarity.EPIC)));

    /**
     * アイテムを登録し、必要ならアイテムタブにも追加する共通メソッド
     *
     * @param name     アイテムIDの名前部分
     * @param function Item.Settings から Item を生成するファクトリ
     * @return 登録済みの Item インスタンス
     */
    private static Item registerAndAddToTab(String name, Function<Item.Settings, Item> function) {
        // Item.Settings から Item を生成
        Item item = Registry.register(
                Registries.ITEM,
                Identifier.of(TokorotenSlimeAPI.getModId(), name),
                function.apply(new Item.Settings().registryKey(
                        RegistryKey.of(RegistryKeys.ITEM, Identifier.of(TokorotenSlimeAPI.getModId(), name)))
                )
        );

        // アイテムタブに追加
        TokorotenSlimeAPI.addItemToTab(item);
        return item;
    }

    /**
     * 登録確認用のログを出力する
     */
    public static void registerModItems() {
        TSSpacetimeTraverse.LOGGER.info("Registering addon items for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
