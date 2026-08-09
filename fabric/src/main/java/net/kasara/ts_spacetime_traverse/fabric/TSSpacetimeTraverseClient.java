package net.kasara.ts_spacetime_traverse.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.client.option.ModKeyMappings;
import net.kasara.ts_spacetime_traverse.client.render.entity.PortalRenderer;
import net.kasara.ts_spacetime_traverse.client.util.ClientAdvancementUtil;
import net.kasara.ts_spacetime_traverse.fabric.block.ModBlocks;
import net.kasara.ts_spacetime_traverse.fabric.client.ModClientEvents;
import net.kasara.ts_spacetime_traverse.fabric.client.network.ClientModPackets;
import net.kasara.ts_spacetime_traverse.fabric.entity.ModEntities;
import net.kasara.ts_spacetime_traverse.fabric.mixin.client.ClientAdvancementManagerAccessor;
import net.minecraft.client.renderer.RenderType;

public class TSSpacetimeTraverseClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Mixinアクセサ経由で実績進捗マップを取得する実装をセット
        ClientAdvancementUtil.GET_PROGRESS_MAP = ca -> ((ClientAdvancementManagerAccessor) ca).getProgress();

        // クライアント専用ネットワーキング登録
        ClientModPackets.register();

        // クライアントイベント登録
        ModClientEvents.register();

        // 位置入れ替えキー登録
        KeyBindingHelper.registerKeyBinding(ModKeyMappings.POSITION_SWAP);

        // ポータル操作キー登録
        KeyBindingHelper.registerKeyBinding(ModKeyMappings.PORTAL_ACTION);

        // ポータルエンティティ描画登録
        EntityRendererRegistry.register(ModEntities.PORTAL, PortalRenderer::new);

        // 半透明描画レイヤー登録(モデルJSONのrender_typeはForge独自拡張のため別途指定が必要)
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.VOID_BLOCK, RenderType.translucent());

        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Mod Key Mappings/Renderers for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }
}
