package net.kasara.ts_spacetime_traverse.forge.item;

import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.item.SpacetimeEyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TokorotenSlimeCommon.MOD_ID);

    // 時空の目アイテムの登録
    public static final RegistryObject<Item> SPACETIME_EYE = registerItemAndAddToTab("spacetime_eye",
            pros -> new SpacetimeEyeItem(pros.fireResistant().rarity(Rarity.EPIC)));

    private static RegistryObject<Item> registerItemAndAddToTab(String name, Function<Item.Properties, Item> factory) {
        RegistryObject<Item> item = ITEMS.register(name, () -> factory.apply(new Item.Properties()));

        // API経由でグループに追加
        TokorotenSlimeAPI.addItemToTab(item);
        return item;
    }

    /**
     * 登録確認用のログを出力するメソッド
     */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);

        // ログ出力
        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Mod Items for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }
}
