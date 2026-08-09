package net.kasara.ts_spacetime_traverse.server;

import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * サーバー側で管理するプレイヤーごとのアクティブポータル
 */
public final class PortalManager {

    // プレイヤー UUID をキーにしたアクティブポータルのマップ
    private static final Map<UUID, PortalEntity> activePlacePortals = new HashMap<>();

    /**
     * 指定プレイヤーの現在アクティブなポータルを取得
     *
     * @param owner プレイヤーUUID
     * @return アクティブポータル、未設定ならnull
     */
    public static PortalEntity getActivePlacePortal(UUID owner) {
        return activePlacePortals.get(owner);
    }

    /**
     * 指定プレイヤーのアクティブポータルを設定
     * 既存のポータルがあれば上書き
     *
     * @param owner プレイヤーUUID
     * @param portal 登録するPortalEntity
     */
    public static void setActivePlacePortal(UUID owner, PortalEntity portal) {
        activePlacePortals.put(owner, portal);
    }

    /**
     * 登録済みポータルと一致する場合のみマップから削除
     *
     * @param owner プレイヤーUUID
     * @param portal 削除対象のPortalEntity
     */
    public static void clearIfMatch(UUID owner, PortalEntity portal) {
        PortalEntity mapPortal = activePlacePortals.get(owner);
        if (portal.equals(mapPortal)) {
            activePlacePortals.remove(owner);
        }
    }

    /**
     * 指定プレイヤーのアクティブポータルをマップから削除
     *
     * @param owner プレイヤーUUID
     * @return 削除されたPortalEntity、存在しなければnull
     */
    public static PortalEntity removeActivePlacePortals(UUID owner) {
        return activePlacePortals.remove(owner);
    }

    /**
     * ポータルが存在するディメンションをtickし続けさせる(サーバーtickから毎tick呼ぶ)
     *
     */
    public static void keepPortalDimensionsActive() {
        for (PortalEntity portal : activePlacePortals.values()) {
            keepAlive(portal);
            keepAlive(portal.getLinkedPortal());
        }
    }

    private static void keepAlive(PortalEntity portal) {
        if (portal == null || portal.isRemoved()) return;
        if (portal.level() instanceof ServerLevel serverLevel) {
            serverLevel.resetEmptyTime();
        }
    }
}
