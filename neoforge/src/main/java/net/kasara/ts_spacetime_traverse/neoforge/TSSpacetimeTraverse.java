package net.kasara.ts_spacetime_traverse.neoforge;

import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.block.ModBlocksCommon;
import net.kasara.ts_spacetime_traverse.block.entity.ModBlockEntitiesCommon;
import net.kasara.ts_spacetime_traverse.entity.ModEntitiesCommon;
import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.neoforge.block.ModBlocks;
import net.kasara.ts_spacetime_traverse.neoforge.block.entity.ModBlockEntities;
import net.kasara.ts_spacetime_traverse.neoforge.entity.ModEntities;
import net.kasara.ts_spacetime_traverse.neoforge.item.ModItems;
import net.kasara.ts_spacetime_traverse.neoforge.network.ModPackets;
import net.kasara.ts_spacetime_traverse.neoforge.server.ModServerEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(TSSpacetimeTraverse.MOD_ID)
public class TSSpacetimeTraverse {

    public static final String MOD_ID = TSSpacetimeTraverseCommon.MOD_ID;
    public static final Logger LOGGER = TSSpacetimeTraverseCommon.LOGGER;

    public TSSpacetimeTraverse(IEventBus modEventBus) {
        // アイテム登録
        ModItems.register(modEventBus);

        // ブロック登録
        ModBlocks.register(modEventBus);

        // ブロックエンティティ登録
        ModBlockEntities.register(modEventBus);

        // エンティティ登録
        ModEntities.register(modEventBus);

        // パケット登録
        ModPackets.register(modEventBus);

        // サーバーイベント登録
        ModServerEvents.register();

        // レジストリ登録の完了後にcommonのブリッジへ反映する
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        ModBlocksCommon.VOID_BLOCK = ModBlocks.VOID_BLOCK.get();
        ModBlockEntitiesCommon.VOID_BE = ModBlockEntities.VOID_BE.get();
        ModEntitiesCommon.PORTAL = ModEntities.PORTAL.get();

        // サーバーからクライアントへの送信ブリッジ
        ModPacketsCommon.SEND_TO_PLAYER = ModPackets::sendToPlayer;
    }
}
