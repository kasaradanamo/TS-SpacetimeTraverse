package net.kasara.ts_spacetime_traverse.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kasara.ts_spacetime_traverse.client.data.WaypointClientCache;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.ApplyWaypointChangeC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.RegisterQuickC2SPacket;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * クライアント側のウェイポイント管理クラス
 */
@Environment(EnvType.CLIENT)
public class WaypointClientManager {

    private WaypointClientManager() {}

    /**
     * ウェイポイントの追加・更新・削除をサーバーへ送信、キャッシュも更新
     *
     * @param data 更新対象のWaypointData
     * @param delete trueなら削除、falseなら追加/更新
     */
    public static void applyWaypointChange(WaypointData data, boolean delete) {
        if (delete) {
            WaypointClientCache.remove(data.uuid());
        } else {
            WaypointClientCache.upsert(data);
        }
        ApplyWaypointChangeC2SPacket.send(data, delete);
    }

    /**
     * クイックウェイポイントとして登録
     *
     * @param dataUuid 登録するWaypointのUUID
     */
    public static void registerQuick(UUID dataUuid) {
        WaypointClientCache.setQuick(dataUuid);
        RegisterQuickC2SPacket.send(dataUuid);
    }

    /**
     * クライアントウェイポイントの初期設定
     *
     * @param waypoints サーバーから送られてきた全ウェイポイント
     * @param quickUuid サーバーが指定するクイックウェイポイントのUUID
     */
    public static void waypointInfo(List<WaypointData> waypoints, @Nullable UUID quickUuid) {
        WaypointClientCache.clear();
        WaypointClientCache.setAll(waypoints);
        WaypointClientCache.setQuick(quickUuid);
    }
}
