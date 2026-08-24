package net.kasara.ts_spacetime_traverse.neoforge;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.client.option.ModKeyMappingsCommon;
import net.kasara.ts_spacetime_traverse.client.render.entity.PortalRenderer;
import net.kasara.ts_spacetime_traverse.client.util.ClientAdvancementUtil;
import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.neoforge.client.ModClientEvents;
import net.kasara.ts_spacetime_traverse.neoforge.client.option.ModKeyMappings;
import net.kasara.ts_spacetime_traverse.neoforge.entity.ModEntities;
import net.kasara.ts_spacetime_traverse.neoforge.mixin.client.ClientAdvancementManagerAccessor;
import net.kasara.ts_spacetime_traverse.neoforge.network.ModPackets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(value = TSSpacetimeTraverse.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TSSpacetimeTraverse.MOD_ID, value = Dist.CLIENT)
public class TSSpacetimeTraverseClient {

    public TSSpacetimeTraverseClient(IEventBus modEventBus) {
        // クライアント→サーバー送信ブリッジ
        ModPacketsCommon.SEND_TO_SERVER = ModPackets::sendToServer;

        ModKeyMappingsCommon.POSITION_SWAP = ModKeyMappings.POSITION_SWAP;
        ModKeyMappingsCommon.PORTAL_ACTION = ModKeyMappings.PORTAL_ACTION;

        // 実績進捗取得ブリッジ(Mixinアクセサ経由)
        ClientAdvancementUtil.GET_PROGRESS_MAP = ca -> ((ClientAdvancementManagerAccessor) ca).getProgress();

        // クライアントイベント登録
        ModClientEvents.register();

        // キーマッピング登録
        modEventBus.addListener(this::registerKeys);

        // レンダリング登録
        modEventBus.addListener(this::registerRenderers);
    }

    private void registerKeys(RegisterKeyMappingsEvent event) {
        // 位置入れ替えキー登録
        event.register(ModKeyMappings.POSITION_SWAP);
        // ポータル操作キー登録
        event.register(ModKeyMappings.PORTAL_ACTION);

        // 登録完了ログを出力
        TSSpacetimeTraverse.LOGGER.info("Registering addon Key Mappings for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // ポータルエンティティの描画
        event.registerEntityRenderer(ModEntities.PORTAL.get(), PortalRenderer::new);

        // 登録完了ログを出力
        TSSpacetimeTraverse.LOGGER.info("Registering addon Renderers for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
