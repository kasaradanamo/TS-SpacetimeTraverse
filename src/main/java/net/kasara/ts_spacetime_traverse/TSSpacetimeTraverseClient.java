package net.kasara.ts_spacetime_traverse;

import net.fabricmc.api.ClientModInitializer;
import net.kasara.ts_spacetime_traverse.client.ModClientEvents;
import net.kasara.ts_spacetime_traverse.entity.ModEntities;
import net.kasara.ts_spacetime_traverse.client.render.entity.PortalRenderer;
import net.kasara.ts_spacetime_traverse.network.ModPackets;
import net.kasara.ts_spacetime_traverse.client.option.ModKeyMappings;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class TSSpacetimeTraverseClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // カスタムキー入力イベントの登録
        ModKeyMappings.register();

        // クライアントイベントの登録
        ModClientEvents.registerEvents();

        // ポータルエンティティのレンダラー登録
        EntityRenderers.register(ModEntities.PORTAL, PortalRenderer::new);

        // サーバー→クライアントパケットの登録
        ModPackets.registerS2CPackets();
    }
}
