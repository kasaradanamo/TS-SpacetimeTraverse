package net.kasara.ts_spacetime_traverse.server;

import net.kasara.ts_spacetime_traverse.entity.ModEntitiesCommon;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.kasara.ts_spacetime_traverse.util.WaypointDataUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * サーバー側でのポータル生成・消滅・リンク管理を担当するクラス
 * ポータルの設置
 * 既存ポータルの消去
 * ポータル侵入時の戻りポータル生成
 */
public class PortalHandler {

    /**
     * 指定されたWaypointに向かうポータルを設置
     * 既に設置済みのポータルがあれば、先に消滅させる
     */
    public static void placePortal(UUID waypointUuid, ServerPlayer player) {
        // 既存の設置ポータルを消滅させる
        vanishOwnedPortals(player);

        ServerLevel level = player.serverLevel();

        // Waypoint情報取得
        WaypointData data = WaypointServerManager.get(player, waypointUuid);
        if (data == null) return;

        // ポータル設置位置を探索
        Vec3 pos = findPortalSpawnPos(player, level, data);
        if (pos == null) {
            // 設置できなかった場合は失敗メッセージを表示
            player.sendSystemMessage(Component.translatable("message.tokorotenslime.portal_failed"), true);
            return;
        }

        // ポータル生成
        PortalEntity portal = spawnPortal(player, level, pos, data, null);

        // アクティブな設置ポータルとして登録
        PortalManager.setActivePlacePortal(player.getUUID(), portal);
    }

    /**
     * プレイヤーが所有しているポータルを消滅アニメーション付きで削除
     */
    public static void vanishOwnedPortals(ServerPlayer player) {
        PortalEntity portal = PortalManager.getActivePlacePortal(player.getUUID());
        if (portal == null) return;
        if (!(player.getUUID().equals(portal.getOwnerUuid()))) return;

        portal.startVanish(portal.level().getGameTime());

        // リンクされている戻りポータルも同時に消す
        PortalEntity linkPortal = portal.getLinkedPortal();
        if (linkPortal == null || !(player.getUUID().equals(linkPortal.getOwnerUuid()))) return;
        linkPortal.startVanish(linkPortal.level().getGameTime());
    }

    /**
     * プレイヤー切断時などに、所有している全ポータルを即座に削除
     */
    public static void discardOwnedPortals(MinecraftServer server, UUID ownerUuid) {
        PortalManager.removeActivePlacePortals(ownerUuid);
        for (ServerLevel level : server.getAllLevels()) {
            for (PortalEntity portal : level.getEntities(ModEntitiesCommon.PORTAL, p -> ownerUuid.equals(p.getOwnerUuid()))) {
                portal.discard();
            }
        }
    }

    /**
     * サーバー終了時に全ポータルを削除する
     */
    public static void discardAllPortals(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (PortalEntity portal : level.getEntities(ModEntitiesCommon.PORTAL, p -> true)) {
                portal.discard();
            }
        }
    }

    /**
     * ポータル侵入時に、戻り用ポータルを生成する処理
     *
     * @param inEntity ポータルに入ったエンティティ
     * @param enteredPortal 入ってきたポータル
     */
    public static void handlePortalEntry(Entity inEntity, PortalEntity enteredPortal) {
        if (enteredPortal.isVanishing()) return;

        UUID portalOwnerUuid = enteredPortal.getOwnerUuid();

        // 既にリンクポータルが存在していれば生成しない
        PortalEntity linked = enteredPortal.getLinkedPortal();
        if (linked != null && !linked.isVanishing() && !linked.isRemoved()) return;

        ServerLevel targetWorld = inEntity.level().getServer()
                .getLevel(enteredPortal.getTargetDimension());
        if (targetWorld == null) return;

        // 元ポータルの背後に戻りWaypointを生成
        BlockPos entryPos = enteredPortal.blockPosition();
        float enteredBackYaw = roundYawToCardinal(enteredPortal.getYRot() + 180f);

        Vec3 backOffset1 = Vec3.directionFromRotation(0, enteredBackYaw).normalize().scale(2);
        BlockPos backPos = BlockPos.containing(entryPos.getX() + backOffset1.x, entryPos.getY(), entryPos.getZ() + backOffset1.z);

        WaypointData data = WaypointDataUtil.fromInputs(
                null,
                "Back Portal",
                enteredPortal.level().dimension(),
                backPos,
                (int) enteredBackYaw
        );

        // 行き先側に戻りポータルを設置
        BlockPos onBlockPos = enteredPortal.getTargetBlockPos();
        Vec3 onPos = new Vec3(onBlockPos.getX() + 0.5, onBlockPos.getY(), onBlockPos.getZ() + 0.5);
        float inEntityBackYaw = roundYawToCardinal(enteredPortal.getTargetYaw() + 180f);
        Vec3 backOffset2 = Vec3.directionFromRotation(0, inEntityBackYaw).normalize().scale(1.5);
        Vec3 backPortalPos = new Vec3(onPos.x + backOffset2.x, onPos.y, onPos.z + backOffset2.z);

        if (hasOwnerPortalNearby(targetWorld, backPortalPos, portalOwnerUuid, 2)
                || !canSpawnPortalAt(targetWorld, backPortalPos)) return;

        PortalEntity returnPortal = spawnPortal(inEntity, targetWorld, backPortalPos, data, enteredPortal);
        if (returnPortal == null) return;

        // 相互リンク
        returnPortal.setLinkPortal(enteredPortal, false);
        enteredPortal.setLinkPortal(returnPortal, true);
    }

    /**
     * ポータルエンティティを生成してワールドにスポーンさせる
     *
     * @param entity ポータルを出す基準となるエンティティ
     * @param level スポーンさせるワールド
     * @param pos スポーン位置
     * @param waypoint 行き先Waypoint情報
     * @param enteredPortal 既存ポータルから生成されたかどうか（戻りポータル用）
     */
    private static PortalEntity spawnPortal(Entity entity, ServerLevel level, Vec3 pos, WaypointData waypoint, @Nullable PortalEntity enteredPortal) {
        PortalEntity portal = new PortalEntity(ModEntitiesCommon.PORTAL, level);

        // プレイヤーが新しく設置するポータル
        // (26.2のmoveOrInterpolateToは1.20.1のmoveToに相当)
        if (entity instanceof ServerPlayer player && enteredPortal == null) {
            portal.moveTo(pos.x, pos.y - 1.5, pos.z, player.getYRot(), 0);
            portal.setOwner(player);
            portal.setWaypoint(waypoint);
        }
        // 戻りポータルとして生成される場合
        else if (enteredPortal != null) {
            portal.moveTo(pos.x, pos.y, pos.z, waypoint.yaw(), 0);
            portal.setOwner(enteredPortal.getOwnerUuid(), enteredPortal.getOwnerName());
            portal.setWaypoint(waypoint);
        } else {
            return null;
        }
        level.addFreshEntity(portal);
        return portal;
    }

    /**
     * プレイヤーの視線方向を基準に、ポータルを設置できる位置を探索
     * 行き先のWaypointと被らないようにする制約も含む
     */
    private static @Nullable Vec3 findPortalSpawnPos(ServerPlayer player, ServerLevel level, WaypointData data) {
        BlockPos targetPos = data.blockPos();

        // 行き先ブロック付近には設置できない
        AABB forbiddenBox = new AABB(
                targetPos.getX() - 0.2, targetPos.getY() - 0.2, targetPos.getZ() - 0.2,
                targetPos.getX() + 1.2, targetPos.getY() + 2.2, targetPos.getZ() + 1.2
        );

        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 eyePos = player.getEyePosition();

        // 前方距離と高さの候補
        double[] forwardOffsets = {2.0, 1.8, 1.6, 1.4, 1.2, 1.0};
        double[] heightOffsets = {0, 0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, -0.2, -0.4, 1.6, 1.8, 2.0, -0.6, -0.8, -1.0};

        // 設置可能な位置を総当たりで探索
        for (double f : forwardOffsets) {
            for (double h : heightOffsets) {
                Vec3 pos = eyePos.add(0, h, 0).add(look.scale(f));
                if (forbiddenBox.contains(pos)) continue;

                AABB box = new AABB(
                        pos.x - 0.2, pos.y - 1.5, pos.z - 0.2,
                        pos.x + 0.2, pos.y + 0.2, pos.z + 0.2
                );

                if (!level.noCollision(box)) continue;
                if (!level.getEntities(null, box).isEmpty()) return null;

                return pos;
            }
        }
        return null;
    }

    /**
     * 指定位置周辺に、同一プレイヤー所有のポータルが存在するかを判定
     */
    private static boolean hasOwnerPortalNearby(ServerLevel level, Vec3 center, UUID ownerUuid, double radius) {
        PortalEntity placePortal = PortalManager.getActivePlacePortal(ownerUuid);
        if (placePortal == null || placePortal.isRemoved()) return false;

        PortalEntity backPortal = placePortal.getLinkedPortal();

        AABB box = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );

        boolean hasPlace = !level.getEntitiesOfClass(
                PortalEntity.class, box, portal -> portal == placePortal
        ).isEmpty();

        boolean hasReturn = backPortal != null && !level.getEntitiesOfClass(
                PortalEntity.class, box, portal -> portal == backPortal
        ).isEmpty();

        return hasPlace || hasReturn;
    }

    /**
     * 指定位置にポータルをスポーン可能か判定
     */
    private static boolean canSpawnPortalAt(ServerLevel level, Vec3 center) {
        AABB portalBox = new AABB(
                center.x - 0.2, center.y + 1.0, center.z - 0.2,
                center.x + 0.2, center.y + 1.7, center.z + 0.2
        );
        return level.getEntities((Entity) null, portalBox, entity -> !(entity instanceof PortalEntity)).isEmpty();
    }

    /**
     * yawを東西南北の4方向にする
     */
    private static float roundYawToCardinal(float yaw) {
        yaw = normalizeYaw(yaw);

        if (yaw >= -45f && yaw < 45f) return 0f;        // 南
        else if (yaw >= 45f && yaw < 135f) return 90f; // 西
        else if (yaw >= -135f && yaw < -45f) return -90f; // 東
        else return 180f;                                   // 北
    }

    /**
     * yawを-180～180で正規化
     */
    private static float normalizeYaw(float yaw) {
        if (yaw > 180f) return yaw - 360f;
        if (yaw < -180f) return yaw + 360f;
        return yaw;
    }
}
