package net.kasara.ts_spacetime_traverse.client;

import net.kasara.ts_spacetime_traverse.client.data.WaypointClientCache;
import net.kasara.ts_spacetime_traverse.client.gui.screen.PortalActionScreen;
import net.kasara.ts_spacetime_traverse.client.util.ClientAdvancementUtil;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PlacePortalC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.VanishPortalC2SPacket;
import net.kasara.ts_spacetime_traverse.client.option.ModKeyMappingsCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * クライアント側でポータル操作に関するキー入力とアクション処理をするハンドラ
 */
public class PortalActionClientHandler {

    // キー押下を1回の入力として扱うためのフラグ（押しっぱなし防止）
    private static boolean keyPressed = false;

    /**
     * キーアクションを処理
     */
    public static void handlePortalAction(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null) return;

        if (!pushKey()) return; // 押された瞬間のみtrue

        // 対応する実績を解除してない場合使用不可
        if (!ClientAdvancementUtil.hasUnlockedSpacetimeAdvancement(minecraft)) return;

        // Ctrlキーが押されてるかどうか
        boolean ctrlPressed = GLFW.glfwGetKey(minecraft.getWindow().handle(),
                GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;

        if (ctrlPressed) {
            minecraft.setScreenAndShow(new PortalActionScreen()); // GUIを開く
        } else {
            // 通常押下:視線上にポータルがあれば消去、なければ設置
            PortalEntity lookedPortal = getLookedPortal(minecraft, player);
            if (lookedPortal != null) {
                VanishPortalC2SPacket.send();
            } else {
                placePortal(player, WaypointClientCache.getQuick());
            }
        }
    }

    /**
     * フレーム内でキーが押されたか判定
     * @return 押されてたらtrue、押されてなかったらfalse
     */
    private static boolean pushKey() {
        boolean isPressed = ModKeyMappingsCommon.PORTAL_ACTION.isDown();

        if (isPressed && !keyPressed) {
            keyPressed = true;
            return true;
        } else if (!isPressed) {
            keyPressed = false;
        }

        return false;
    }

    /**
     * プレイヤーの視線上にある自分が保有するポータルを取得
     *
     * @param minecraft マインクラフトインスタンス
     * @param player    視線判定を行うプレイヤー
     * @return 視線上のポータルエンティティ(なければnull)
     */
    private static @Nullable PortalEntity getLookedPortal(Minecraft minecraft, Player player) {
        if (minecraft.level == null) return null;

        double maxDistance = 64.0;

        // 視線の開始点（カメラ位置）と方向ベクトル
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getViewVector(1.0f);
        Vec3 end = start.add(direction.scale(maxDistance));

        // 視線方向に伸ばした範囲でポータルを検索
        AABB searchBox = player.getBoundingBox().expandTowards(direction.scale(maxDistance)).inflate(0);

        // 自分がオーナーのポータルのみを対象
        List<PortalEntity> portals = minecraft.level.getEntitiesOfClass(
                PortalEntity.class,
                searchBox,
                portal -> player.getUUID().equals(portal.getOwnerUuid())
        );
        if (portals.isEmpty()) return null;

        // 当たり判定を少しずつ広げる
        float[] expands = {0, 0.4f, 0.7f};

        for (double expand : expands) {
            PortalEntity nearestPortal = null;
            double nearestDistSq = Double.MAX_VALUE;

            for (PortalEntity portal : portals) {
                AABB portalBox = portal.getBoundingBox().inflate(expand);

                // 視線レイとポータルの当たり判定をチェック
                Optional<Vec3> hit = portalBox.clip(start, end);
                if (hit.isPresent()) {
                    // 一番近いヒット点をもつポータルを選択
                    double distSq = hit.get().distanceToSqr(start);
                    if (distSq < nearestDistSq) {
                        nearestDistSq = distSq;
                        nearestPortal = portal;
                    }
                }
            }

            // このサイズで見つかったら確定
            if (nearestPortal != null) {
                return nearestPortal;
            }
        }
        return null;
    }

    /**
     * WaypointUUIDを指定してポータルを設置する
     *
     * @param player メッセージ対象のプレイヤー
     * @param waypointUuid ポータルに保存するWaypointデータのUUID
     */
    public static void placePortal(Player player, UUID waypointUuid) {
        // Waypoint未指定の場合はエラーメッセージ表示
        if (waypointUuid == null) {
            player.sendOverlayMessage(Component.translatable("message.tokorotenslime.not_place_portal"));
            return;
        }
        // サーバーに設置要求
        PlacePortalC2SPacket.send(waypointUuid);
    }
}
