package net.kasara.ts_spacetime_traverse.neoforge.client;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.client.PortalActionClientHandler;
import net.kasara.ts_spacetime_traverse.client.PositionSwapClientHandler;
import net.kasara.ts_spacetime_traverse.neoforge.TSSpacetimeTraverse;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * クライアント側のイベント登録クラス
 */
public class ModClientEvents {

    /**
     * イベントをまとめて登録
     */
    public static void register() {
        NeoForge.EVENT_BUS.register(ModClientEvents.class);

        // ログ
        TSSpacetimeTraverse.LOGGER.info("Registering addon Client Events for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }

    // クライアントの毎Tick終了時に呼ばれるイベント
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        // 位置入れ替え系のキー入力・処理
        PositionSwapClientHandler.handleSwapPositions(minecraft);

        // ポータル操作系のキー入力・処理
        PortalActionClientHandler.handlePortalAction(minecraft);
    }
}
