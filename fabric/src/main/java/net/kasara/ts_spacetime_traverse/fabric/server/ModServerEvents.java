package net.kasara.ts_spacetime_traverse.fabric.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.WaypointInfoS2CPacket;
import net.kasara.ts_spacetime_traverse.server.DimensionListSender;
import net.kasara.ts_spacetime_traverse.server.PortalHandler;
import net.kasara.ts_spacetime_traverse.server.PortalManager;
import net.kasara.ts_spacetime_traverse.server.WaypointServerManager;
import net.minecraft.server.level.ServerPlayer;

/**
 * サーバー側の各種イベントを登録するクラス
 */
public class ModServerEvents {

    /**
     * サーバー関連イベントの登録処理
     */
    public static void register() {
        // プレイヤーがサーバーに参加したときの処理
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;

            // クライアントにウェイポイント情報を送る
            WaypointInfoS2CPacket.send(player, WaypointServerManager.getAll(player), WaypointServerManager.getQuick(player));

            // クライアントにディメンションリストを送る
            DimensionListSender.send(player.serverLevel().getServer(), player);
        });

        // プレイヤーがサーバーから切断したときの処理
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;

            // 所有しているポータルを全て破棄
            PortalHandler.discardOwnedPortals(player.serverLevel().getServer(), player.getUUID());
        });

        // サーバーtickごとにポータルのあるディメンションを稼働させ続ける
        ServerTickEvents.END_SERVER_TICK.register(server -> PortalManager.keepPortalDimensionsActive());

        // ボーダーが変わったtickの終わりに送る
        ServerTickEvents.END_SERVER_TICK.register(DimensionListSender::sendIfBordersChanged);

        // サーバーが起動したとき
        ServerLifecycleEvents.SERVER_STARTED.register(DimensionListSender::watchBorders);

        // サーバーが終了するとき
        ServerLifecycleEvents.SERVER_STOPPING.register(PortalHandler::discardAllPortals);

        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Server Events for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }
}
