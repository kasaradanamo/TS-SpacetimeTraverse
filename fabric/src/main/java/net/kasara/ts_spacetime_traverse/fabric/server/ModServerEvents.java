package net.kasara.ts_spacetime_traverse.fabric.server;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.fabric.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.WaypointInfoS2CPacket;
import net.kasara.ts_spacetime_traverse.server.DimensionListSender;
import net.kasara.ts_spacetime_traverse.server.PortalHandler;
import net.kasara.ts_spacetime_traverse.server.PortalManager;
import net.kasara.ts_spacetime_traverse.server.WaypointServerManager;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

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
            ServerPlayer player = handler.getPlayer();
            // クライアントにウェイポイント情報を送る
            WaypointInfoS2CPacket.send(player, WaypointServerManager.getAll(player), WaypointServerManager.getQuick(player));

            // クライアントにディメンションリストを送る
            DimensionListSender.send(server, player);
        });

        // プレイヤーがサーバーから切断したときの処理
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID uuid = handler.getPlayer().getUUID();
            // 所有しているポータルを全て破棄
            PortalHandler.discardOwnedPortals(server, uuid);
        });

        // サーバーtickごとにポータルのあるディメンションを稼働させ続ける
        ServerTickEvents.END_SERVER_TICK.register(server -> PortalManager.keepPortalDimensionsActive());

        // ボーダーが変わったtickの終わりに送る
        ServerTickEvents.END_SERVER_TICK.register(DimensionListSender::sendIfBordersChanged);

        // サーバーが起動したとき
        ServerLifecycleEvents.SERVER_STARTED.register(DimensionListSender::watchBorders);

        // サーバーが終了するとき
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            // 全てのポータルを削除
            PortalHandler.discardAllPortals(server);
        });

        // 死亡やエンドからの帰還でプレイヤーが作り直されるとき
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            // Waypointデータを新しいプレイヤーに引き継ぐ
            WaypointServerManager.copyFrom(oldPlayer, newPlayer);
        });

        // ログ
        TSSpacetimeTraverse.LOGGER.info("Registering addon Server Events for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
