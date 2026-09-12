package net.kasara.ts_spacetime_traverse.neoforge.block;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.block.VoidBlock;
import net.kasara.ts_spacetime_traverse.neoforge.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.neoforge.item.ModItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(TokorotenSlimeAPI.getModId());

    // ポータルくぐった際足元何もなかった時に出てくる一時ブロック
    public static final DeferredBlock<Block> VOID_BLOCK =
            registerBlock("void_block", pros -> new VoidBlock(pros
                    .strength(-1.0F, 3600000.0F)    // 岩盤と同じ
                    .noLootTable()                      // ドロップなし
                    .noOcclusion()                      // 透過ブロック
                    .isValidSpawn((state, level, pos, entityType) -> false)        // スポーン不可
                    .isRedstoneConductor((state, level, pos) -> false) // 衝突判定なし
                    .isSuffocating((state, level, pos) -> false)       // 窒息判定なし
                    .isViewBlocking((state, level, pos) -> false)      // 視界を遮らない
            ));

    private static DeferredBlock<Block> registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory) {
        DeferredBlock<Block> block = BLOCKS.registerBlock(name, factory);
        // BlockItemを登録
        registerBlockItem(name, block);
        return block;
    }

    private static void registerBlockItem(String name, DeferredBlock<Block> block) {
        ModItems.ITEMS.registerSimpleBlockItem(name, block);
    }

    /**
     * ModBlocksの登録処理を呼び出す
     */
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);

        TSSpacetimeTraverse.LOGGER.info("Registering addon Blocks for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
