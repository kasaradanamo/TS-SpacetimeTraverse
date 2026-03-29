package net.kasara.ts_spacetime_traverse.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kasara.ts_spacetime_traverse.client.util.ClientAdvancementUtil;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PositionSwapC2SPacket;
import net.kasara.ts_spacetime_traverse.client.option.ModKeyBindings;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PositionSwapModeC2SPacket;
import net.kasara.ts_spacetime_traverse.util.ModTags;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * クライアント側でスワップ操作に関するキー入力とアクション処理をするハンドラ
 */
@Environment(EnvType.CLIENT)
public class PositionSwapClientHandler {

    // モード切替キーが押下中かどうか（押下判定の連打防止用）
    private static boolean keyPressed = false;

    private static final Random RANDOM = new Random();

    /**
     * 毎ティックClientEventsから呼ばれる
     */
    public static void handleSwapPositions(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null || !pushKey()) return;

        // 周りのエンティティを取得
        List<Entity> rangeEntities = new ArrayList<>();
        getRangeEntities(client, player, rangeEntities);

        // 視線に最も近いエンティティを取得
        Entity target = selectEntity(player, rangeEntities);
        if (target == null) return;

        // 同じエンティティを二度と選ばないように除外
        rangeEntities.remove(target);

        // 4ブロック以内だったら、他の候補から交換相手を再選択
        boolean random = false;
        if (!rangeEntities.isEmpty() && player.squaredDistanceTo(target) <= 16) {
            Entity randomTarget = divideRangeEntities(player, rangeEntities);
            if (randomTarget != null) {
                target = randomTarget;
                random = true;
            }
        }

        // 選択したエンティティIDとエンティティのいるディメンション名とランダムbooleanをサーバー送信して入れ替え処理
        PositionSwapC2SPacket.send(target.getId(), target.getEntityWorld().getRegistryKey(), random);
    }

    /**
     * キーの押し始めのみtrue
     */
    private static boolean pushKey() {
        // 現在のフレームでキーが押されているかどうかを取得
        boolean isPressed = ModKeyBindings.POSITION_SWAP.isPressed();

        if (isPressed && !keyPressed) {
            keyPressed = true;

            boolean ctrlPressed = GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(),
                    GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;

            // ctrlキーが押されていた場合
            if (ctrlPressed) {
                // 進捗確認
                if (!ClientAdvancementUtil.hasUnlockedSpacetimeAdvancement(MinecraftClient.getInstance())) return false;

                // モード切替するだけ
                PositionSwapModeC2SPacket.send();
                return false;
            }
            return true;
        } else if (!isPressed) keyPressed = false;
        return false;
    }

    /**
     * プレイヤーの周囲30ブロック以内で、視線が通ってるエンティティをフィルタし、
     * 草などの視線を防げないブロックは無視される
     */
    private static void getRangeEntities(MinecraftClient client, PlayerEntity player, List<Entity> rangeEntities) {
        World world = client.world;
        if (world == null) return;

        Box searchBox = player.getBoundingBox().expand(30);
        for (Entity entity : world.getOtherEntities(player, searchBox)) {
            if (entity instanceof PortalEntity) continue;               // 特定のエンティティ判定(ポータルは除外)
            if (hasObstacleInSight(client, entity, player)) continue;   // ブロックの判定
            rangeEntities.add(entity);
        }
    }

    /**
     * プレイヤーとターゲットの間に遮るブロックがあるかを判定
     * 草など一部のブロックは除外されてる
     */
    private static boolean hasObstacleInSight(MinecraftClient client, Entity targetEntity, PlayerEntity player) {
        Vec3d start = player.getEyePos();
        Vec3d end = targetEntity.getEyePos();

        // プレイヤー視点からターゲットまでの直線上にある最初のブロックを取得
        BlockHitResult hit = client.world.raycast(new RaycastContext(
                start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));

        if (hit.getType() != HitResult.Type.BLOCK) return false;

        BlockState blockState = client.world.getBlockState(hit.getBlockPos());
        Identifier id = Registries.BLOCK.getId(blockState.getBlock());

        // 通過可能なブロックを指定
        return !blockState.isIn(ModTags.Blocks.PASS_THROUGH_BLOCK)
                && !id.getPath().contains("vine")
                && !id.getPath().contains("bush");
    }

    /**
     * 視線上で最も近いエンティティを選択
     */
    private static Entity selectEntity(PlayerEntity player, List<Entity> rangeEntities) {
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(player.getRotationVec(1.0F).multiply(30.0));

        Entity closestEntity = null;
        double closestDistanceSqr = 900;    // 最大30ブロック

        // 矢を一時的に貯めるリスト
        List<Entity> arrowCandidates = new ArrayList<>();

        // ---1回目の判定---
        for (Entity entity : rangeEntities) {
            if (entity == player) continue;

            // 矢は後回し
            if (entity.getType().isIn(EntityTypeTags.IMPACT_PROJECTILES)) {
                arrowCandidates.add(entity);
                continue;
            }

            Optional<Vec3d> hitPos = entity.getBoundingBox().raycast(start, end);
            if(hitPos.isPresent()) {
                double distanceSqr = start.squaredDistanceTo(hitPos.get());
                if (distanceSqr < closestDistanceSqr) {
                    closestDistanceSqr = distanceSqr;
                    closestEntity = entity;
                }
            }
        }

        // ---2回目の判定（矢用）---
        if (closestEntity == null) {
            for (Entity arrow : arrowCandidates) {
                Optional<Vec3d> hitPos = arrow.getBoundingBox().expand(1.5F).raycast(start, end);   // 矢だけ広めにする
                if (hitPos.isPresent()) {
                    double distance = start.distanceTo(hitPos.get());
                    if (distance < closestDistanceSqr) {
                        closestDistanceSqr = distance;
                        closestEntity = arrow;
                    }
                }
            }
        }

        return closestEntity;
    }

    /**
     * 距離で分類し、10～20ブロックの中距離帯があればその中からランダム、それ以外は全体からランダム選択
     * リストからランダムに1体選ぶ。ただし、プレイヤーは除外（他プレイヤーを誤って選ばないように）
     */
    private static Entity divideRangeEntities(PlayerEntity player, List<Entity> rangeEntities) {
        // プレイヤーが乗ってるエンティティ
        Entity playerVehicle = player.getVehicle();

        Entity midCandidate = null;
        int midCount = 0;

        Entity fallbackCandidate = null;
        int fallbackCount = 0;

        for (Entity entity : rangeEntities) {
            // プレイヤー自身とプレイヤーの乗ってるエンティティは除外
            if (entity instanceof PlayerEntity || entity == playerVehicle) continue;

            double d = player.squaredDistanceTo(entity);

            // 10ブロック以上、20ブロック以下の範囲
            if (d > 100 && d <= 400) {
                midCount++;
                if (RANDOM.nextInt(midCount) == 0) {
                    midCandidate = entity;
                }
            } else {
                fallbackCount++;
                if (RANDOM.nextInt(fallbackCount) == 0) {
                    fallbackCandidate = entity;
                }
            }
        }

        if (midCandidate != null) return midCandidate;
        return fallbackCandidate;
    }
}
