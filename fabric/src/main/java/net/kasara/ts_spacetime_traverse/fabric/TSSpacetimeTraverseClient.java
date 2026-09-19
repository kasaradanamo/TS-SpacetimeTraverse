package net.kasara.ts_spacetime_traverse.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.entity.EntityRenderers;

import net.kasara.ts_spacetime_traverse.client.option.ModKeyMappingsCommon;
import net.kasara.ts_spacetime_traverse.client.render.entity.PortalRenderer;
import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.fabric.client.ModClientEvents;
import net.kasara.ts_spacetime_traverse.fabric.client.option.ModKeyMappings;
import net.kasara.ts_spacetime_traverse.fabric.entity.ModEntities;
import net.kasara.ts_spacetime_traverse.fabric.network.ModPackets;

public class TSSpacetimeTraverseClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // クライアント→サーバー送信ブリッジ(クライアント専用APIのためここで代入)
        ModPacketsCommon.SEND_TO_SERVER = ClientPlayNetworking::send;

        // キーマッピング登録
        ModKeyMappings.register();
        ModKeyMappingsCommon.POSITION_SWAP = ModKeyMappings.POSITION_SWAP;
        ModKeyMappingsCommon.PORTAL_ACTION = ModKeyMappings.PORTAL_ACTION;

        // クライアントイベント登録
        ModClientEvents.register();

        // ポータルエンティティ描画登録
        EntityRenderers.register(ModEntities.PORTAL, PortalRenderer::new);

        // S2Cパケット登録
        ModPackets.registerS2CPackets();
    }
}
