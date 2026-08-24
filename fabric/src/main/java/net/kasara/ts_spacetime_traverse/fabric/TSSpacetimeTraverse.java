package net.kasara.ts_spacetime_traverse.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.block.ModBlocksCommon;
import net.kasara.ts_spacetime_traverse.block.entity.ModBlockEntitiesCommon;
import net.kasara.ts_spacetime_traverse.entity.ModEntitiesCommon;
import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.fabric.block.ModBlocks;
import net.kasara.ts_spacetime_traverse.fabric.block.entity.ModBlockEntities;
import net.kasara.ts_spacetime_traverse.fabric.entity.ModEntities;
import net.kasara.ts_spacetime_traverse.fabric.item.ModItems;
import net.kasara.ts_spacetime_traverse.fabric.network.ModPackets;
import net.kasara.ts_spacetime_traverse.fabric.server.ModServerEvents;
import org.slf4j.Logger;

public class TSSpacetimeTraverse implements ModInitializer {

    public static final String MOD_ID = TSSpacetimeTraverseCommon.MOD_ID;
    public static final Logger LOGGER = TSSpacetimeTraverseCommon.LOGGER;

    @Override
    public void onInitialize() {
        // アイテム登録
        ModItems.register();

        // ブロック登録
        ModBlocks.register();
        ModBlocksCommon.VOID_BLOCK = ModBlocks.VOID_BLOCK;

        // ブロックエンティティ登録
        ModBlockEntities.register();
        ModBlockEntitiesCommon.VOID_BE = ModBlockEntities.VOID_BE;

        // エンティティ登録
        ModEntities.register();
        ModEntitiesCommon.PORTAL = ModEntities.PORTAL;

        // C2Sパケット登録
        ModPackets.registerPayloadTypes();
        ModPackets.registerC2SPackets();
        // サーバー側からクライアントへの送信ブリッジ(共通エントリポイントで代入)
        ModPacketsCommon.SEND_TO_PLAYER = ServerPlayNetworking::send;

        // サーバーイベント登録
        ModServerEvents.register();
    }
}
