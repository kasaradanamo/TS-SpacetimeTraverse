package net.kasara.ts_spacetime_traverse.fabric.block;

import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.block.ModBlocksCommon;
import net.kasara.ts_spacetime_traverse.block.VoidBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class ModBlocks {

    // ポータルくぐった際足元何もなかった時に出てくる一時ブロック
    public static final Block VOID_BLOCK = registerBlock("void_block", pros -> new VoidBlock(pros
            .strength(-1.0F, 3600000.0F)    // 岩盤と同じ
            .noLootTable()                      // ドロップなし
            .noOcclusion()                      // 透過ブロック
            .isValidSpawn((state, level, pos, type) -> false)   // スポーン不可
            .isRedstoneConductor((state, level, pos) -> false)  // レッドストーン非導通
            .isSuffocating((state, level, pos) -> false)        // 窒息判定なし
            .isViewBlocking((state, level, pos) -> false)       // 視界を遮らない
    ));

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory) {
        ResourceLocation id = new ResourceLocation(TokorotenSlimeCommon.MOD_ID, name);
        Block block = factory.apply(BlockBehaviour.Properties.of());

        registerBlockItem(id, block);

        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    private static void registerBlockItem(ResourceLocation blockId, Block block) {
        Registry.register(BuiltInRegistries.ITEM, blockId, new BlockItem(block, new Item.Properties()));
    }

    /**
     * ModBlocksの登録処理を呼び出す
     */
    public static void register() {
        ModBlocksCommon.VOID_BLOCK = VOID_BLOCK;

        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon blocks for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }
}
