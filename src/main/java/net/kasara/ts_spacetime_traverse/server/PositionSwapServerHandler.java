package net.kasara.ts_spacetime_traverse.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

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
    public static void positionSwap(int targetId, RegistryKey<World> dimension, ServerPlayerEntity player, boolean random) {
        // ワールド取得
        ServerWorld playerWorld = player.getEntityWorld();
        ServerWorld targetWorld = playerWorld.getServer().getWorld(dimension);
        if (targetWorld == null) return;    // ワールドが存在しなければ終了

        // 強化モードかどうか
        boolean isEnhanced = PositionSwapModeManager.get(player).equals("enhanced");

        // ターゲットエンティティ取得
        Entity target = targetWorld.getEntityById(targetId);
        if (target == null || target == player) return;    // ターゲットが存在しないor自分自身なら終了

        // プレイヤーとターゲットの現在位置を取得
        Vec3d playerPos = player.getEntityPos();
        Vec3d targetPos = target.getEntityPos();

        // プレイヤーの角度（yaw/pitch）を決定
        // randomがtrueならターゲットからプレイヤー方向を向く角度を計算
        // 投射物の場合はそのまま、それ以外はターゲットの角度を使用
        float[] newPlayerAngles = (random) ? lookTarget(playerPos, targetPos) : (target instanceof PersistentProjectileEntity)
                ? new float[]{player.getYaw(), player.getPitch()} : new float[]{target.getYaw(), target.getPitch()};

        // ターゲットの角度を決定
        // 投射物の場合はそのまま、それ以外はプレイヤーの角度を使用
        float[] newTargetAngles = (target instanceof PersistentProjectileEntity)
                ? new float[]{target.getYaw(), target.getPitch()} : new float[]{player.getYaw(), player.getPitch()};

        // 速度
        Vec3d playerMotion = target instanceof WindChargeEntity && isEnhanced ? new Vec3d(0, -2, 0) : player.getVelocity();
        Vec3d targetMotion = target.getVelocity();

        // 高さ
        double playerFall = player.fallDistance;
        double targetFall = target.fallDistance;

        // 空中かどうか
        boolean playerGround = player.isOnGround();
        boolean targetGround = target.isOnGround();

        // ポーズ
        EntityPose playerPose = player.getPose();
        EntityPose targetPose = target.getPose();

        // 乗ってる対象取得
        Entity playerVehicle = player.getVehicle();
        Entity targetVehicle = target.getVehicle();

        // 乗り物から降ろす
        player.stopRiding();
        target.stopRiding();

        // 位置交換
        // クライアントに同期
        player.networkHandler.requestTeleport(targetPos.x, targetPos.y, targetPos.z, newPlayerAngles[0], newPlayerAngles[1]);
        player.velocityDirty = true;
        if (isEnhanced) {
            swap(player, targetWorld, targetPos, newPlayerAngles[0], newPlayerAngles[1], targetMotion, targetFall, targetGround, targetPose, targetVehicle);
            swap(target, playerWorld, playerPos, newTargetAngles[0], newTargetAngles[1], playerMotion, playerFall, playerGround, playerPose, playerVehicle);
        } else {
            swap(player, targetWorld, targetPos, newPlayerAngles[0], newPlayerAngles[1], playerMotion, targetFall, targetGround, targetPose, targetVehicle);
            swap(target, playerWorld, playerPos, newTargetAngles[0], newTargetAngles[1], targetMotion, playerFall, playerGround, playerPose, playerVehicle);
        }

        // テレポート音を再生（エンダーマンの音）
        playerWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5F, 1.0F);
        targetWorld.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5F, 1.0F);
    }

    /**
     * 2点間の方向を向くためのyaw/pitchを計算
     *
     * @param playerPos 起点の座標
     * @param targetPos   目標座標
     * @return {yaw, pitch}
     */
    private static float[] lookTarget(Vec3d playerPos, Vec3d targetPos) {
        Vec3d delta = playerPos.subtract(targetPos);
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
     * @param world 移動先ワールド
     * @param pos 移動先座標
     * @param yaw 回転角度(水平)
     * @param pitch 回転角度(垂直)
     * @param motion 速度
     * @param fallDistance 高さレベル
     * @param ground 飛んでるか
     * @param pose ポーズ(スニーク状態か、泳いでるかなど)
     * @param vehicle 騎乗エンティティ
     */
    private static void swap(Entity entity, World world, Vec3d pos, float yaw, float pitch, Vec3d motion, double fallDistance, boolean ground, EntityPose pose, @Nullable Entity vehicle) {

        // テレポート情報
        TeleportTarget target = new TeleportTarget(
                (ServerWorld) world,
                pos,
                motion,
                yaw,
                pitch,
                false,
                false,
                Set.of(),
                TeleportTarget.ADD_PORTAL_CHUNK_TICKET
        );

        entity.teleportTo(target);          // 位置や向きなど適応
        entity.fallDistance = fallDistance; // 高さ適応
        entity.setOnGround(ground);         // 空中かどうか適応
        entity.setPose(pose);               // ポーズ適応

        // 騎乗関係再構築
        if (vehicle != null && entity instanceof LivingEntity) {
            entity.startRiding(vehicle, true, false);

            // 乗り物同期
            ServerWorld vehicleWorld = (ServerWorld) vehicle.getEntityWorld();
            vehicleWorld.getChunkManager().sendToNearbyPlayers(vehicle, new EntityPassengersSetS2CPacket(vehicle));
        }
    }
}
