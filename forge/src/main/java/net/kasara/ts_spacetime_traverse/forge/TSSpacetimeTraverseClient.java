package net.kasara.ts_spacetime_traverse.forge;

import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.client.util.ClientAdvancementUtil;
import net.kasara.ts_spacetime_traverse.client.option.ModKeyMappings;
import net.kasara.ts_spacetime_traverse.client.render.entity.PortalRenderer;
import net.kasara.ts_spacetime_traverse.forge.client.ModClientEvents;
import net.kasara.ts_spacetime_traverse.forge.entity.ModEntities;
import net.kasara.ts_spacetime_traverse.forge.mixin.client.ClientAdvancementManagerAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TSSpacetimeTraverseCommon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TSSpacetimeTraverseClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Mixinアクセサ経由で実績進捗マップを取得する実装をセット
            ClientAdvancementUtil.GET_PROGRESS_MAP = ca -> ((ClientAdvancementManagerAccessor) ca).getProgress();

            // クライアントイベント登録
            ModClientEvents.register();
        });
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        // 位置入れ替えキー登録
        event.register(ModKeyMappings.POSITION_SWAP);

        // ポータル操作キー登録
        event.register(ModKeyMappings.PORTAL_ACTION);

        // 登録完了ログを出力
        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Mod Key Mappings for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // ポータルエンティティ描画登録
        event.registerEntityRenderer(ModEntities.PORTAL.get(), PortalRenderer::new);

        // 登録完了ログを出力
        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Mod Renderers for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }
}
