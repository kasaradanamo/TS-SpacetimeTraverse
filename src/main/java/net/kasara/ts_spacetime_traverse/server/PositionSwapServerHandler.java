package net.kasara.ts_spacetime_traverse.server;

import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * プレイヤーとターゲットエンティティの位置を入れ替える処理
 */
public class PositionSwapServerHandler {

    /**
     * 指定ターゲットとプレイヤーの位置を交換する
     *
     * @param targetId 対象エンティティのID
     * @param dimension 対象エンティティが存在するワールドの名前
     * @param player 操作するプレイヤー
     * @param random ランダム補正を行うか
     */
    public static void positionSwap(int targetId, ResourceKey<Level> dimension, ServerPlayer player, boolean random) {
        // レベル取得
        ServerLevel playerLevel = player.level();
        ServerLevel targetLevel = playerLevel.getServer().getLevel(dimension);
        if (targetLevel == null) return;    // レベルが存在しなければ終了

        // 強化モードかどうか
        boolean isEnhanced = PositionSwapModeManager.get(player).equals("enhanced");

        // ターゲットエンティティ取得
        Entity target = targetLevel.getEntity(targetId);
        if (target == null || target == player) return;    // ターゲットが存在しないor自分自身なら終了

        // プレイヤーとターゲットの現在位置を取得
        Vec3 playerPos = player.position();
        Vec3 targetPos = target.position();

        // プレイヤーの角度（yaw/pitch）を決定
        // randomがtrueならターゲットからプレイヤー方向を向く角度を計算
        // 投射物の場合はそのまま、それ以外はターゲットの角度を使用
        float[] newPlayerAngles = (random) ? lookTarget(playerPos, targetPos) : (target instanceof AbstractArrow)
                ? new float[]{player.getYRot(), player.getXRot()} : new float[]{target.getYRot(), target.getXRot()};

        // ターゲットの角度を決定
        // 投射物の場合はそのまま、それ以外はプレイヤーの角度を使用
        float[] newTargetAngles = (target instanceof AbstractArrow)
                ? new float[]{target.getYRot(), target.getXRot()} : new float[]{player.getYRot(), player.getXRot()};

        // 速度
        Vec3 playerMotion = target instanceof WindCharge && isEnhanced ? new Vec3(0, -2, 0) : player.getDeltaMovement();
        Vec3 targetMotion = target.getDeltaMovement();

        // 高さ
        double playerFall = player.fallDistance;
        double targetFall = target.fallDistance;

        // 空中かどうか
        boolean playerGround = player.onGround();
        boolean targetGround = target.onGround();

        // ポーズ
        Pose playerPose = player.getPose();
        Pose targetPose = target.getPose();

        // 乗ってる対象取得
        Entity playerVehicle = player.getVehicle();
        Entity targetVehicle = target.getVehicle();

        // 乗り物から降ろす
        player.stopRiding();
        target.stopRiding();

        // 位置交換
        // クライアントに同期
        player.connection.teleport(targetPos.x, targetPos.y, targetPos.z, newPlayerAngles[0], newPlayerAngles[1]);
        player.hurtMarked = true;
        if (isEnhanced) {
            swap(player, targetLevel, targetPos, newPlayerAngles[0], newPlayerAngles[1], targetMotion, targetFall, targetGround, targetPose, targetVehicle);
            swap(target, playerLevel, playerPos, newTargetAngles[0], newTargetAngles[1], playerMotion, playerFall, playerGround, playerPose, playerVehicle);
        } else {
            swap(player, targetLevel, targetPos, newPlayerAngles[0], newPlayerAngles[1], playerMotion, targetFall, targetGround, targetPose, targetVehicle);
            swap(target, playerLevel, playerPos, newTargetAngles[0], newTargetAngles[1], targetMotion, playerFall, playerGround, playerPose, playerVehicle);
        }

        // テレポート音を再生(エンダーマンの音)
        playerLevel.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.0F);
        targetLevel.playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    /**
     * 2点間の方向を向くためのyaw/pitchを計算
     *
     * @param playerPos 起点の座標
     * @param targetPos   目標座標
     * @return {yaw, pitch}
     */
    private static float[] lookTarget(Vec3 playerPos, Vec3 targetPos) {
        Vec3 delta = playerPos.subtract(targetPos);
        double dx = delta.x, dy = delta.y, dz = delta.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (Math.toDegrees(Math.atan2(-dx, dz)));
        float pitch = (float) (Math.toDegrees(-Math.atan2(dy, horizontalDistance)));

        return new float[]{yaw, pitch};
    }

    /**
     * エンティティをテレポートさせる
     *
     * @param entity 移動させるエンティティ
     * @param level 移動先レベル
     * @param pos 移動先座標
     * @param yaw 回転角度(水平)
     * @param pitch 回転角度(垂直)
     * @param motion 速度
     * @param fallDistance 高さレベル
     * @param ground 飛んでるか
     * @param pose ポーズ(スニーク状態か、泳いでるかなど)
     * @param vehicle 騎乗エンティティ
     */
    private static void swap(Entity entity, Level level, Vec3 pos, float yaw, float pitch, Vec3 motion, double fallDistance, boolean ground, Pose pose, @Nullable Entity vehicle) {

        // テレポート情報
        TeleportTransition transition = new TeleportTransition(
                (ServerLevel) level,
                pos,
                motion,
                yaw,
                pitch,
                false,
                false,
                Set.of(),
                TeleportTransition.PLACE_PORTAL_TICKET
        );

        entity.teleport(transition);        // 位置や向きなど適応
        entity.fallDistance = fallDistance; // 高さ適応
        entity.setOnGround(ground);         // 空中かどうか適応
        entity.setPose(pose);               // ポーズ適応

        // 騎乗関係再構築
        if (vehicle != null && entity instanceof LivingEntity) {
            entity.startRiding(vehicle, true, false);

            // 乗り物同期
            ServerLevel vehicleLevel = (ServerLevel) vehicle.level();
            vehicleLevel.getChunkSource().sendToTrackingPlayersAndSelf(vehicle, new ClientboundSetPassengersPacket(vehicle));
        }
    }
}