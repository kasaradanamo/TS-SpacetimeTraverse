package net.kasara.ts_spacetime_traverse.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Set;

/**
 * プレイヤーとターゲットエンティティの位置を入れ替える処理
 */
public class SwapPositionHandler {

    /**
     * 指定ターゲットとプレイヤーの位置を交換する
     *
     * @param targetId 対象エンティティのID
     * @param dimensionName 対象エンティティが存在するワールドの名前
     * @param player 操作するプレイヤー
     * @param random ランダム補正を行うか
     */
    public static void swapPosition(int targetId, String dimensionName, ServerPlayerEntity player, boolean random) {
        ServerWorld playerWorld = player.getEntityWorld();
        ServerWorld targetWorld = playerWorld.getServer().getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimensionName)));
        if (targetWorld == null) return;    // ワールドが存在しなければ終了
        Entity target = targetWorld.getEntityById(targetId);
        if (target == null || target.equals(player)) return;    // ターゲットが存在しないor自分自身なら終了

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

        // 位置交換
        tpEntity(player, targetWorld, targetPos, newPlayerAngles[0], newPlayerAngles[1]);
        tpEntity(target, playerWorld, playerPos, newTargetAngles[0], newTargetAngles[1]);

        // テレポート音を再生（エンダーマンの音）
        playerWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5F, 1.0F);
        targetWorld.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5F, 1.0F);
    }

    /**
     * エンティティを指定ワールド・座標・角度にテレポートさせる
     * プレイヤーの場合はクライアント側も同期
     *
     * @param entity 移動させるエンティティ
     * @param world  移動先ワールド
     * @param pos    移動先座標
     * @param yaw    回転角度（水平）
     * @param pitch  回転角度（垂直）
     */
    private static void tpEntity(Entity entity, World world, Vec3d pos, float yaw, float pitch) {
        Set<PositionFlag> flags = EnumSet.noneOf(PositionFlag.class);

        entity.teleport((ServerWorld) world, pos.x, pos.y, pos.z, flags, yaw, pitch, false);

        if (entity instanceof ServerPlayerEntity player) {
            player.networkHandler.requestTeleport(pos.x, pos.y, pos.z, yaw, pitch);
        }
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
}
