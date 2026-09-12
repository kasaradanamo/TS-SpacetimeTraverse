package net.kasara.ts_spacetime_traverse.fabric.block;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.block.VoidBlock;
import net.kasara.ts_spacetime_traverse.fabric.TSSpacetimeTraverse;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class ModBlocks {

    // ポータルくぐった際足元何もなかった時に出てくる一時ブロック
    public static final Block VOID_BLOCK = registerBlock("void_block", pros -> new VoidBlock(pros
            .strength(-1.0F, 3600000.0F)    // 岩盤と同じ
            .noLootTable()                      // ドロップなし
            .noOcclusion()                      // 透過ブロック
            .isValidSpawn(Blocks::never)        // スポーン不可
            .isRedstoneConductor(Blocks::never) // 衝突判定なし
            .isSuffocating(Blocks::never)       // 窒息判定なし
            .isViewBlocking(Blocks::never)      // 視界を遮らない
    ));

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TokorotenSlimeAPI.getModId(), name);
        Block block = factory.apply(BlockBehaviour.Properties.of());
        // BlockItem を登録
        registerBlockItem(id, block);
        // ブロック本体をレジストリに登録
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    private static void registerBlockItem(ResourceLocation blockId, Block block) {
        Registry.register(
                BuiltInRegistries.ITEM,
                blockId,
                new BlockItem(block, new Item.Properties())
        );
    }

    /**
     * 登録確認ログ出力
     */
    public static void register() {
        TSSpacetimeTraverse.LOGGER.info("Registering addon Blocks for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
