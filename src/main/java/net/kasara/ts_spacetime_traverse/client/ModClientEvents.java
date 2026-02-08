package net.kasara.ts_spacetime_traverse.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;

/**
 * クライアント側のイベント登録クラス
 */
@Environment(EnvType.CLIENT)
public class ModClientEvents {

    /**
     * ライアントイベントをまとめて登録する
     */
    public static void registerEvents() {
        // クライアントの毎Tick終了時に呼ばれるイベント
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // 位置入れ替え系のキー入力・処理
            SwapPositionsClientHandler.handleSwapPositions(client);

            // ポータル操作系のキー入力・処理
            PortalActionClientHandler.handlePortalAction(client);
        });

        // ログ
        TSSpacetimeTraverse.LOGGER.info("Registering addon Client Events for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
