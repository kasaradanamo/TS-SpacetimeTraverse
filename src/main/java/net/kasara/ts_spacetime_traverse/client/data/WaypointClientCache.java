package net.kasara.ts_spacetime_traverse.client.data;

import net.kasara.ts_spacetime_traverse.util.WaypointData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * クライアント側でのウェイポイントキャッシュ
 */
public final class WaypointClientCache {

    //  UUIDをキーにしたウェイポイントマップ（順序保持）
    private static final Map<UUID, WaypointData> waypointMap = new LinkedHashMap<>();

    // クイックウェイポイントのUUID
    private static UUID quickUuid = null;

    private WaypointClientCache() {}

    /**
     * 全ウェイポイントを取得
     */
    public static List<WaypointData> getAll() {
        return new ArrayList<>(waypointMap.values());
    }

    /**
     * 指定UUIDのウェイポイントを取得
     *
     * @param uuid waypointのUUID
     * @return 該当がなければnull
     */
    public static @Nullable WaypointData get(UUID uuid) {
        return waypointMap.get(uuid);
    }

    /**
     * クイックウェイポイントのUUIDを取得
     *
     * @return クイックUUID、未設定ならnull
     */
    public static @Nullable UUID getQuick() {
        return quickUuid;
    }

    /**
     * キャッシュを丸ごと更新
     *
     * @param waypoints 新しいウェイポイントリスト
     */
    public static void setAll(List<WaypointData> waypoints) {
        waypointMap.clear();
        for (WaypointData wp : waypoints) {
            waypointMap.put(wp.uuid(), wp);
        }
    }

    /**
     * クイックウェイポイントを設定または解除
     *
     * @param uuid 設定するUUID、解除する場合はnull
     */
    public static void setQuick(@Nullable UUID uuid) {
        quickUuid = uuid;
    }

    /**
     * ウェイポイントを追加または更新
     * 既存 UUIDがあれば上書き
     * 最初の追加の場合は自動でクイックに設定
     *
     * @param data 追加・更新する WaypointData
     */
    public static void upsert(WaypointData data) {
        waypointMap.put(data.uuid(), data);
        // 最初の追加の場合は自動でクイックウェイポイントに設定
        if (waypointMap.size() == 1 && quickUuid == null) {
            quickUuid = data.uuid();
        }
    }

    /**
     * UUID に対応するウェイポイントを削除
     * クイックと一致していれば nullにリセット
     *
     * @param uuid 削除対象のUUID
     */
    public static void remove(UUID uuid) {
        waypointMap.remove(uuid);
        if (uuid.equals(quickUuid)) {
            quickUuid = null;
        }
    }

    /**
     * キャッシュを完全にクリア
     */
    public static void clear() {
        waypointMap.clear();
        quickUuid = null;
    }
}
