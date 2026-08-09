package net.kasara.ts_spacetime_traverse.forge.client;

import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.client.PortalActionClientHandler;
import net.kasara.ts_spacetime_traverse.client.PositionSwapClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * クライアント側のイベント登録クラス
 */
public class ModClientEvents {

    /**
     * イベントをまとめて登録
     */
    public static void register() {
        MinecraftForge.EVENT_BUS.register(ModClientEvents.class);

        // ログ
        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Client Events for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }

    // クライアントの毎Tick終了時に呼ばれるイベント
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        // 位置入れ替え系のキー入力・処理
        PositionSwapClientHandler.handleSwapPositions(minecraft);

        // ポータル操作系のキー入力・処理
        PortalActionClientHandler.handlePortalAction(minecraft);
    }
}
