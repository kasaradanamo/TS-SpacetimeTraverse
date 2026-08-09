package net.kasara.ts_spacetime_traverse.fabric;

import net.fabricmc.api.ModInitializer;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.fabric.block.ModBlocks;
import net.kasara.ts_spacetime_traverse.fabric.block.entity.ModBlockEntities;
import net.kasara.ts_spacetime_traverse.fabric.entity.ModEntities;
import net.kasara.ts_spacetime_traverse.fabric.item.ModItems;
import net.kasara.ts_spacetime_traverse.fabric.network.ModPackets;
import net.kasara.ts_spacetime_traverse.fabric.server.ModServerEvents;

public class TSSpacetimeTraverse implements ModInitializer {

    public static final String MOD_ID = TSSpacetimeTraverseCommon.MOD_ID;

    @Override
    public void onInitialize() {
        // アイテム登録
        ModItems.register();

        // ブロック登録
        ModBlocks.register();

        // ブロックエンティティ登録
        ModBlockEntities.register();

        // エンティティ登録
        ModEntities.register();

        // ネットワーキング登録
        ModPackets.register();

        // サーバーイベント登録
        ModServerEvents.register();
    }
}
