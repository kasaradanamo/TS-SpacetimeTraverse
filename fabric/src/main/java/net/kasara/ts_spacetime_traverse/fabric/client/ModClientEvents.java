package net.kasara.ts_spacetime_traverse.fabric.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.client.PortalActionClientHandler;
import net.kasara.ts_spacetime_traverse.client.PositionSwapClientHandler;
import net.minecraft.client.Minecraft;

/**
 * クライアント側のイベント登録クラス
 */
public class ModClientEvents {

    /**
     * イベントをまとめて登録
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if (minecraft.player == null) return;

            // 位置入れ替え系のキー入力・処理
            PositionSwapClientHandler.handleSwapPositions(minecraft);

            // ポータル操作系のキー入力・処理
            PortalActionClientHandler.handlePortalAction(minecraft);
        });

        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Client Events for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }
}
