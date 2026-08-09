package net.kasara.ts_spacetime_traverse.forge.block;

import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.block.ModBlocksCommon;
import net.kasara.ts_spacetime_traverse.block.VoidBlock;
import net.kasara.ts_spacetime_traverse.forge.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TokorotenSlimeCommon.MOD_ID);

    // ポータルくぐった際足元何もなかった時に出てくる一時ブロック
    public static final RegistryObject<Block> VOID_BLOCK = registerBlock("void_block", pros -> new VoidBlock(pros
            .strength(-1.0F, 3600000.0F)    // 岩盤と同じ
            .noLootTable()                      // ドロップなし
            .noOcclusion()                      // 透過ブロック
            .isValidSpawn((state, level, pos, type) -> false)   // スポーン不可
            .isRedstoneConductor((state, level, pos) -> false)  // レッドストーン非導通
            .isSuffocating((state, level, pos) -> false)        // 窒息判定なし
            .isViewBlocking((state, level, pos) -> false)       // 視界を遮らない
    ));

    private static RegistryObject<Block> registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> factory.apply(BlockBehaviour.Properties.of()));
        // BlockItem を登録
        registerBlockItem(name, block);
        return block;
    }

    private static void registerBlockItem(String name, RegistryObject<Block> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    /**
     * 登録確認ログ出力
     */
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);

        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon blocks for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }

    /**
     * commonホルダーへ登録済みインスタンスを反映する(FMLCommonSetupEventから呼び出す)
     */
    public static void initCommonHolder() {
        ModBlocksCommon.VOID_BLOCK = VOID_BLOCK.get();
    }
}
