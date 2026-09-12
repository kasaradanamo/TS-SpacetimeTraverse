package net.kasara.ts_spacetime_traverse.fabric.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.client.PortalActionClientHandler;
import net.kasara.ts_spacetime_traverse.client.PositionSwapClientHandler;
import net.kasara.ts_spacetime_traverse.fabric.TSSpacetimeTraverse;

/**
 * クライアント側のイベント登録クラス
 */
public class ModClientEvents {

    /**
     * イベントをまとめて登録
     */
    public static void register() {
        // クライアントの毎Tick終了時に呼ばれるイベント
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if (minecraft.player == null) return;

            // 位置入れ替え系のキー入力・処理
            PositionSwapClientHandler.handleSwapPositions(minecraft);

            // ポータル操作系のキー入力・処理
            PortalActionClientHandler.handlePortalAction(minecraft);
        });

        // ログ
        TSSpacetimeTraverse.LOGGER.info("Registering addon Client Events for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
