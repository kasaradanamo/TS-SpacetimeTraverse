package net.kasara.ts_spacetime_traverse.neoforge.server;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.neoforge.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.WaypointInfoS2CPacket;
import net.kasara.ts_spacetime_traverse.server.DimensionListSender;
import net.kasara.ts_spacetime_traverse.server.PortalHandler;
import net.kasara.ts_spacetime_traverse.server.WaypointServerManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.UUID;

/**
 * サーバー側の各種イベントを登録するクラス
 */
public class ModServerEvents {

    /**
     * サーバー関連イベントの登録処理
     */
    public static void register() {
        NeoForge.EVENT_BUS.register(ModServerEvents.class);

        // ログ
        TSSpacetimeTraverse.LOGGER.info("Registering addon Server Events for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }

    // プレイヤーがサーバーに参加したときの処理
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // クライアントにnbt情報を伝える
        WaypointInfoS2CPacket.send(player, WaypointServerManager.getAll(player), WaypointServerManager.getQuick(player));

        // クライアントにディメンションリストを送る
        DimensionListSender.send(player.level().getServer(), player);
    }

    // プレイヤーがサーバーから切断したときの処理
    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();
        // 所有しているポータルを全て破棄
        PortalHandler.discardOwnedPortals(player.level().getServer(), uuid);
    }

    // サーバーが終了するとき
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        // 全てのポータルを削除
        PortalHandler.discardAllPortals(event.getServer());
    }

    // プレイヤーエンティティがコピーされるとき(死亡・ディメンション移動など)
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;

        // Waypointデータを新しいプレイヤーに引き継ぐ
        WaypointServerManager.copyFrom(oldPlayer, newPlayer);
    }
}
