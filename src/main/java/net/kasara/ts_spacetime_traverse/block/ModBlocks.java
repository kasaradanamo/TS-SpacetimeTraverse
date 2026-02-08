package net.kasara.ts_spacetime_traverse.block;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

/**
 * Modで追加するブロックの登録管理クラス
 */
public class ModBlocks {

    /** ポータルくぐった際足元何もなかった時に出てくる一時ブロック */
    public static final Block VOID_BLOCK = registerBlock("void_block", properties -> new VoidBlock(properties
            .strength(-1.0F, 3600000.0F)// 破壊不可・耐久極大
            .dropsNothing()                 // ドロップなし
            .nonOpaque()                    // 透過ブロック
            .allowsSpawning(Blocks::never)  // スポーン不可
            .solidBlock(Blocks::never)      // 衝突判定なし
            .suffocates(Blocks::never)      // 窒息判定なし
            .blockVision(Blocks::never)     // 視界を遮らない
    ));

    /**
     * ブロックと対応するBlockItemを登録
     */
    private static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> function) {
        // ブロックのインスタンス生成
        Block toRegister = function.apply(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK,
                Identifier.of(TokorotenSlimeAPI.getModId(), name))));
        // BlockItem を登録
        registerBlockItem(name, toRegister);

        // ブロック本体を登録
        return Registry.register(Registries.BLOCK, Identifier.of(TokorotenSlimeAPI.getModId(), name), toRegister);
    }

    /**
     * ブロックに対応するBlockItemを登録
     */
    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(TokorotenSlimeAPI.getModId(), name),
                new BlockItem(block, new Item.Settings()
                        .useBlockPrefixedTranslationKey()   // 翻訳キーにブロック名を反映
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(TokorotenSlimeAPI.getModId(), name))))
        );
    }

    /**
     * ModBlocksの登録処理を呼び出す
     */
    public static void registerBlocks() {
        TSSpacetimeTraverse.LOGGER.info("Registering addon blocks for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
