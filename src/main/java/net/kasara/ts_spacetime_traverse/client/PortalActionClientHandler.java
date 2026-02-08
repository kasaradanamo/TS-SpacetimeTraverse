package net.kasara.ts_spacetime_traverse.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.client.data.WaypointClientCache;
import net.kasara.ts_spacetime_traverse.client.gui.screen.PortalActionScreen;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.kasara.ts_spacetime_traverse.mixin.ClientAdvancementManagerAccessor;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PlacePortalC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.VanishPortalC2SPacket;
import net.kasara.ts_spacetime_traverse.client.option.ModKeyBindings;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * クライアント側でポータル操作に関するキー入力とアクション処理をするハンドラ
 */
@Environment(EnvType.CLIENT)
public class PortalActionClientHandler {

    // 使用可能かどうかを判定するための実績ID
    private static final Identifier SPACETIME_ADVANCEMENT =
            Identifier.of(TokorotenSlimeAPI.getModId(), "use_spacetime_eye");

    // キー押下を1回の入力として扱うためのフラグ（押しっぱなし防止）
    private static boolean keyPressed = false;

    /**
     * キーアクションを処理
     *
     * @param client マインクラフトインスタンス
     */
    public static void handlePortalAction(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null) return;

        if (!pushKey()) return; // 押された瞬間のみtrue

        // 対応する実績を解除してない場合使用不可
        if (!hasUnlockedSpacetimeAdvancement(client)) return;

        // Ctrlキーが押されてるかどうか
        boolean ctrlPressed = GLFW.glfwGetKey(client.getWindow().getHandle(),
                GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;

        if (ctrlPressed) {
            client.setScreen(new PortalActionScreen()); // GUIを開く
        } else {
            // 通常押下:視線上にポータルがあれば消去、なければ設置
            var lookedPortal = getLookedPortal(client, player);
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
        boolean isPressed = ModKeyBindings.PORTAL_ACTION.isPressed();

        if (isPressed && !keyPressed) {
            keyPressed = true;
            return true;
        } else if (!isPressed) {
            keyPressed = false;
        }

        return false;
    }

    /**
     * クライアント側の実績情報を使って、特定の実績が解除済みかどうか判定
     *
     * @param client マインクラフトインスタンス
     * @return 実績が解除されてればtrue、されてなければfalse
     */
    private static boolean hasUnlockedSpacetimeAdvancement(MinecraftClient client) {
        if (client == null || client.player == null || client.getNetworkHandler() == null) return false;

        var clientAdvancementManager = client.getNetworkHandler().getAdvancementHandler();
        if (clientAdvancementManager == null) return false;

        // mixin経由で進捗マップを取得
        Map<AdvancementEntry, AdvancementProgress> progresses =
                ((ClientAdvancementManagerAccessor) clientAdvancementManager).getAdvancementProgresses();

        AdvancementEntry entry = clientAdvancementManager.get(SPACETIME_ADVANCEMENT);
        if (entry == null) return false;

        AdvancementProgress progress = progresses.get(entry);
        return progress != null && progress.isDone();
    }

    /**
     * プレイヤーの視線上にある自分が保有するポータルを取得
     *
     * @param client マインクラフトインスタンス
     * @param player 視線判定を行うプレイヤー
     * @return 視線上のポータルエンティティ(なければnull)
     */
    private static PortalEntity getLookedPortal(MinecraftClient client, PlayerEntity player) {
        if (client.world == null) return null;

        double maxDistance = 64.0;

        // 視線の開始点（カメラ位置）と方向ベクトル
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d direction = player.getRotationVec(1.0f);
        Vec3d end = start.add(direction.multiply(maxDistance));

        // 視線方向に伸ばした範囲でポータルを検索
        Box searchBox = player.getBoundingBox().stretch(direction.multiply(maxDistance)).expand(0);

        // 自分がオーナーのポータルのみを対象
        List<PortalEntity> portals = client.world.getEntitiesByClass(
                PortalEntity.class,
                searchBox,
                portal -> player.getUuid().equals(portal.getOwnerUuid())
        );
        if (portals.isEmpty()) return null;

        // 当たり判定を少しずつ広げる
        float[] expands = {0, 0.4f, 0.7f};

        for (double expand : expands) {
            PortalEntity nearestPortal = null;
            double nearestDistSq = Double.MAX_VALUE;

            for (PortalEntity portal : portals) {
                Box portalBox = portal.getBoundingBox().expand(expand);

                // 視線レイとポータルの当たり判定をチェック
                Optional<Vec3d> hit = portalBox.raycast(start, end);
                if (hit.isPresent()) {
                    // 一番近いヒット点をもつポータルを選択
                    double distSq = hit.get().squaredDistanceTo(start);
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
    public static void placePortal(PlayerEntity player, UUID waypointUuid) {
        // Waypoint未指定の場合はエラーメッセージ表示
        if (waypointUuid == null) {
            player.sendMessage(Text.translatable("message.tokorotenslime.not_place_portal"), true);
            return;
        }
        // サーバーに設置要求
        PlacePortalC2SPacket.send(waypointUuid);
    }
}
