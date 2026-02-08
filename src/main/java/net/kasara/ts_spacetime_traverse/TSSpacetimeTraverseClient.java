package net.kasara.ts_spacetime_traverse;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.kasara.ts_spacetime_traverse.block.ModBlocks;
import net.kasara.ts_spacetime_traverse.client.ModClientEvents;
import net.kasara.ts_spacetime_traverse.entity.ModEntities;
import net.kasara.ts_spacetime_traverse.client.render.entity.PortalRenderer;
import net.kasara.ts_spacetime_traverse.network.ModPackets;
import net.kasara.ts_spacetime_traverse.client.option.ModKeyBindings;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactories;

public class TSSpacetimeTraverseClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // カスタムキー入力イベントの登録
        ModKeyBindings.register();

        // クライアントイベントの登録
        ModClientEvents.registerEvents();

        // ポータルエンティティのレンダラー登録
        EntityRendererFactories.register(ModEntities.PORTAL, PortalRenderer::new);

        // void_blockの描画レイヤー設定
        BlockRenderLayerMap.putBlock(ModBlocks.VOID_BLOCK, BlockRenderLayer.TRANSLUCENT);

        // サーバー→クライアントパケットの登録
        ModPackets.registerS2CPackets();
    }
}
