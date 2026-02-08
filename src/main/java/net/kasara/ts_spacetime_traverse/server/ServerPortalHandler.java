package net.kasara.ts_spacetime_traverse.server;

import net.kasara.ts_spacetime_traverse.entity.ModEntities;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.kasara.ts_spacetime_traverse.util.WaypointDataFactory;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * サーバー側でのポータル生成・消滅・リンク管理を担当するクラス
 * ポータルの設置
 * 既存ポータルの消去
 * ポータル侵入時の戻りポータル生成
 */
public class ServerPortalHandler {

    /**
     * 指定されたWaypointに向かうポータルを設置
     * 既に設置済みのポータルがあれば、先に消滅させる
     */
    public static void placePortal(UUID waypointUuid, ServerPlayerEntity player) {
        // 既存の設置ポータルを消滅させる
        vanishOwnedPortals(player);

        ServerWorld world = player.getEntityWorld();

        // Waypoint情報取得
        WaypointData data = ServerWaypointManager.get(player, waypointUuid);
        if (data == null) return;

        // ポータル設置位置を探索
        Vec3d pos = findPortalSpawnPos(player, world, data);
        if (pos == null) {
            // 設置できなかった場合は失敗メッセージを表示
            player.sendMessage(Text.translatable("message.tokorotenslime.portal_failed"), true);
            return;
        }

        // ポータル生成
        PortalEntity portal = spawnPortal(player, world, pos, data, null);

        // アクティブな設置ポータルとして登録
        ServerPortalManager.setActivePlacePortal(player.getUuid(), portal);
    }

    /**
     * プレイヤーが所有しているポータルを消滅アニメーション付きで削除
     */
    public static void vanishOwnedPortals(ServerPlayerEntity player) {
        PortalEntity portal = ServerPortalManager.getActivePlacePortal(player.getUuid());
        if (portal == null) return;
        if (!(player.getUuid().equals(portal.getOwnerUuid()))) return;

        portal.startVanish(portal.getEntityWorld().getTime());

        // リンクされている戻りポータルも同時に消す
        PortalEntity linkPortal = portal.getLinkedPortal();
        if (linkPortal == null || !(player.getUuid().equals(linkPortal.getOwnerUuid()))) return;
        linkPortal.startVanish(linkPortal.getEntityWorld().getTime());
    }

    /**
     * プレイヤー切断時などに、所有している全ポータルを即座に削除
     */
    public static void discardOwnedPortals(MinecraftServer server, UUID ownerUuid) {
        ServerPortalManager.removeActivePlacePortals(ownerUuid);
        for (ServerWorld world : server.getWorlds()) {
            for (PortalEntity portal : world.getEntitiesByType(ModEntities.PORTAL, p -> ownerUuid.equals(p.getOwnerUuid()))) {
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

        ServerWorld targetWorld = inEntity.getEntityWorld().getServer()
                .getWorld(enteredPortal.getTargetDimension());
        if (targetWorld == null) return;

        // 元ポータルの背後に戻りWaypointを生成
        BlockPos entryPos = enteredPortal.getBlockPos();
        float enteredBackYaw = roundYawToCardinal(enteredPortal.getYaw() + 180f);

        Vec3d backOffset1 = Vec3d.fromPolar(0, enteredBackYaw).normalize().multiply(2);
        BlockPos backPos = BlockPos.ofFloored(entryPos.getX() + backOffset1.x, entryPos.getY(), entryPos.getZ() + backOffset1.z);

        WaypointData data = WaypointDataFactory.fromInputs(
                null,
                "Back Portal",
                enteredPortal.getEntityWorld().getRegistryKey(),
                backPos,
                (int)enteredBackYaw
        );

        // 行き先側に戻りポータルを設置
        BlockPos onBlockPos = enteredPortal.getTargetBlockPos();
        Vec3d onPos = new Vec3d(onBlockPos.getX() + 0.5, onBlockPos.getY(), onBlockPos.getZ() + 0.5);
        float inEntityBackYaw = roundYawToCardinal(enteredPortal.getTargetYaw() + 180f);
        Vec3d backOffset2 = Vec3d.fromPolar(0, inEntityBackYaw).normalize().multiply(1.5);
        Vec3d backPortalPos = new Vec3d(onPos.getX() + backOffset2.x, onPos.getY(), onPos.getZ() + backOffset2.z);

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
     * @param world スポーンさせるワールド
     * @param pos スポーン位置
     * @param waypoint 行き先Waypoint情報
     * @param enteredPortal 既存ポータルから生成されたかどうか（戻りポータル用）
     */
    private static PortalEntity spawnPortal(Entity entity, ServerWorld world, Vec3d pos, WaypointData waypoint, @Nullable PortalEntity enteredPortal) {
        PortalEntity portal = new PortalEntity(ModEntities.PORTAL, world);

        // プレイヤーが新しく設置するポータル
        if (entity instanceof ServerPlayerEntity player && enteredPortal == null) {
            portal.refreshPositionAndAngles(pos.x, pos.y - 1.5, pos.z, player.getYaw(), 0);
            portal.setOwner(player);
            portal.setWaypoint(waypoint);
        }
        // 戻りポータルとして生成される場合
        else if (enteredPortal != null){
            portal.refreshPositionAndAngles(pos.x, pos.y, pos.z, waypoint.yaw(), 0);
            portal.setOwner(enteredPortal.getOwnerUuid(), enteredPortal.getOwnerName());
            portal.setWaypoint(waypoint);
        } else {
            return null;
        }
        world.spawnEntity(portal);
        return portal;
    }

    /**
     * プレイヤーの視線方向を基準に、ポータルを設置できる位置を探索
     * 行き先のWaypointと被らないようにする制約も含む
     */
    @Nullable
    private static Vec3d findPortalSpawnPos(ServerPlayerEntity player, ServerWorld world, WaypointData data) {
        BlockPos targetPos = data.blockPos();

        // 行き先ブロック付近には設置できない
        Box forbiddenBox = new Box(
                targetPos.getX() - 0.2, targetPos.getY()- 0.2, targetPos.getZ()- 0.2,
                targetPos.getX() + 1.2, targetPos.getY() + 2.2, targetPos.getZ() + 1.2
        );

        Vec3d look = player.getRotationVec(1.0F).normalize();
        Vec3d eyePos = player.getEyePos();

        // 前方距離と高さの候補
        double[] forwardOffsets = {2.0, 1.8, 1.6, 1.4, 1.2, 1.0};
        double[] heightOffsets = {0, 0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, -0.2, -0.4, 1.6, 1.8, 2.0, -0.6, -0.8, -1.0};

        // 設置可能な位置を総当たりで探索
        for (double f : forwardOffsets) {
            for (double h : heightOffsets) {
                Vec3d pos = eyePos.add(0, h, 0).add(look.multiply(f));
                if (forbiddenBox.contains(pos)) continue;

                Box box = new Box(
                        pos.x - 0.2, pos.y - 1.5, pos.z - 0.2,
                        pos.x + 0.2, pos.y + 0.2, pos.z + 0.2
                );

                if (!world.isSpaceEmpty(box)) continue;
                if (!world.getOtherEntities(null, box).isEmpty()) return null;

                return pos;
            }
        }
        return null;
    }

    /**
     * 指定位置周辺に、同一プレイヤー所有のポータルが存在するかを判定
     */
    private static boolean hasOwnerPortalNearby(ServerWorld world, Vec3d center, UUID ownerUuid, double radius) {
        PortalEntity placePortal = ServerPortalManager.getActivePlacePortal(ownerUuid);
        if (placePortal == null || placePortal.isRemoved()) return false;

        PortalEntity backPortal = placePortal.getLinkedPortal();

        Box box = new Box(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );

        boolean hasPlace = !world.getEntitiesByClass(
                PortalEntity.class, box, portal -> portal == placePortal
        ).isEmpty();

        boolean hasReturn = backPortal != null && !world.getEntitiesByClass(
                PortalEntity.class, box, portal -> portal == backPortal
        ).isEmpty();

        return hasPlace || hasReturn;
    }

    /**
     * 指定位置にポータルをスポーン可能か判定
     */
    private static boolean canSpawnPortalAt(ServerWorld world, Vec3d center) {
        Box portalBox = new Box(
                center.x - 0.2, center.y + 1.0, center.z - 0.2,
                center.x + 0.2, center.y + 1.7, center.z + 0.2
        );
        return world.getOtherEntities(null, portalBox, entity -> !(entity instanceof PortalEntity)).isEmpty();
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
