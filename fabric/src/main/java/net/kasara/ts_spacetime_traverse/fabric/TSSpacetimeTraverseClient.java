package net.kasara.ts_spacetime_traverse.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;

import net.kasara.ts_spacetime_traverse.client.option.ModKeyMappingsCommon;
import net.kasara.ts_spacetime_traverse.client.render.entity.PortalRenderer;
import net.kasara.ts_spacetime_traverse.client.util.ClientAdvancementUtil;
import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.fabric.client.ModClientEvents;
import net.kasara.ts_spacetime_traverse.fabric.client.option.ModKeyMappings;
import net.kasara.ts_spacetime_traverse.fabric.block.ModBlocks;
import net.kasara.ts_spacetime_traverse.fabric.entity.ModEntities;
import net.kasara.ts_spacetime_traverse.fabric.mixin.client.ClientAdvancementManagerAccessor;
import net.kasara.ts_spacetime_traverse.fabric.network.ModPackets;

public class TSSpacetimeTraverseClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // クライアント→サーバー送信ブリッジ
        ModPacketsCommon.SEND_TO_SERVER = ClientPlayNetworking::send;

        // キーマッピング登録
        ModKeyMappings.register();
        ModKeyMappingsCommon.POSITION_SWAP = ModKeyMappings.POSITION_SWAP;
        ModKeyMappingsCommon.PORTAL_ACTION = ModKeyMappings.PORTAL_ACTION;

        // 実績進捗取得ブリッジ(Mixinアクセサ経由)
        ClientAdvancementUtil.GET_PROGRESS_MAP = ca -> ((ClientAdvancementManagerAccessor) ca).getProgress();

        // クライアントイベント登録
        ModClientEvents.register();

        // ポータルエンティティ描画登録
        EntityRendererRegistry.register(ModEntities.PORTAL, PortalRenderer::new);

        // 半透明描画レイヤー登録
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.VOID_BLOCK, RenderType.translucent());

        // S2Cパケット登録
        ModPackets.registerS2CPackets();
    }
}
