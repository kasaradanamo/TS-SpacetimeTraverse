package net.kasara.ts_spacetime_traverse;

import net.fabricmc.api.ModInitializer;

import net.kasara.ts_spacetime_traverse.block.ModBlocks;
import net.kasara.ts_spacetime_traverse.block.entity.ModBlockEntities;
import net.kasara.ts_spacetime_traverse.entity.ModEntities;
import net.kasara.ts_spacetime_traverse.item.ModItems;
import net.kasara.ts_spacetime_traverse.network.ModPackets;
import net.kasara.ts_spacetime_traverse.server.ModServerEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TSSpacetimeTraverse implements ModInitializer {
    public static final String MOD_ID = "ts_spacetime_traverse";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // ネットワークパケットタイプとクライアント→サーバーパケットの登録
        ModPackets.registerPayloadTypes();
        ModPackets.registerC2SPackets();

        // アイテム登録
        ModItems.registerModItems();

        // ブロック登録
        ModBlocks.registerBlocks();

        // ブロックエンティティ登録
        ModBlockEntities.registerBlockEntities();

        // エンティティ登録
        ModEntities.registerModEntities();

        // サーバーイベント登録
        ModServerEvents.registerEvents();
    }
}