package net.kasara.ts_spacetime_traverse.neoforge.item;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.item.SpacetimeEyeItem;
import net.kasara.ts_spacetime_traverse.neoforge.TSSpacetimeTraverse;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TokorotenSlimeAPI.getModId());

    // 時空の目アイテムの登録
    public static final DeferredItem<Item> SPACETIME_EYE = registerItemAndAddToTab("spacetime_eye",
            pros -> new SpacetimeEyeItem(pros.fireResistant().rarity(Rarity.EPIC)));

    private static DeferredItem<Item> registerItemAndAddToTab(String name, Function<Item.Properties, Item> factory) {
        DeferredItem<Item> item = ITEMS.registerItem(name, factory);
        // API経由でグループに追加
        TokorotenSlimeAPI.addItemToTab(item);
        return item;
    }

    /**
     * 登録確認用のログ出力
     */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);

        TSSpacetimeTraverse.LOGGER.info("Registering addon Items for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
