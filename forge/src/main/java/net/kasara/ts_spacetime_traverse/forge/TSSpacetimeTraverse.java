package net.kasara.ts_spacetime_traverse.forge;

import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.forge.block.ModBlocks;
import net.kasara.ts_spacetime_traverse.forge.block.entity.ModBlockEntities;
import net.kasara.ts_spacetime_traverse.forge.entity.ModEntities;
import net.kasara.ts_spacetime_traverse.forge.item.ModItems;
import net.kasara.ts_spacetime_traverse.forge.network.ModPackets;
import net.kasara.ts_spacetime_traverse.forge.server.ModServerEvents;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TSSpacetimeTraverseCommon.MOD_ID)
public class TSSpacetimeTraverse {

    public static final String MOD_ID = TSSpacetimeTraverseCommon.MOD_ID;

    public TSSpacetimeTraverse() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // アイテム登録
        ModItems.register(modEventBus);

        // ブロック登録
        ModBlocks.register(modEventBus);

        // ブロックエンティティ登録
        ModBlockEntities.register(modEventBus);

        // エンティティ登録
        ModEntities.register(modEventBus);

        // ネットワーキング登録
        ModPackets.register();

        // サーバーイベント登録
        ModServerEvents.register();

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        ModBlocks.initCommonHolder();
        ModBlockEntities.initCommonHolder();
        ModEntities.initCommonHolder();
    }
}
