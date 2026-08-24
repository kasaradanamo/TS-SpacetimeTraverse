package net.kasara.ts_spacetime_traverse.client;

import net.kasara.ts_spacetime_traverse.client.util.ClientAdvancementUtil;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PositionSwapModeC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PositionSwapC2SPacket;
import net.kasara.ts_spacetime_traverse.client.option.ModKeyMappingsCommon;
import net.kasara.ts_spacetime_traverse.util.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * クライアント側でスワップ操作に関するキー入力とアクション処理をするハンドラ
 */
public class PositionSwapClientHandler {

    // モード切替キーが押下中かどうか（押下判定の連打防止用）
    private static boolean keyPressed = false;

    private static final Random RANDOM = new Random();

    /**
     * 毎ティックClientEventsから呼ばれる
     */
    public static void handleSwapPositions(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null || !pushKey()) return;

        // 周りのエンティティを取得
        List<Entity> rangeEntities = new ArrayList<>();
        getRangeEntities(minecraft, player, rangeEntities);

        // 視線に最も近いエンティティを取得
        Entity target = selectEntity(player, rangeEntities);
        if (target == null) return;

        // 同じエンティティを二度と選ばないように除外
        rangeEntities.remove(target);

        // 4ブロック以内だったら、他の候補から交換相手を再選択
        boolean random = false;
        if (!rangeEntities.isEmpty() && player.distanceToSqr(target) <= 16) {
            Entity randomTarget = divideRangeEntities(player, rangeEntities);
            if (randomTarget != null) {
                target = randomTarget;
                random = true;
            }
        }

        // 選択したエンティティIDとエンティティのいるディメンション名とランダムbooleanをサーバー送信して入れ替え処理
        PositionSwapC2SPacket.send(target.getId(), target.level().dimension(), random);
    }

    /**
     * キーの押し始めのみtrue
     */
    private static boolean pushKey() {
        // 現在のフレームでキーが押されているかどうかを取得
        boolean isPressed = ModKeyMappingsCommon.POSITION_SWAP.isDown();

        if (isPressed && !keyPressed) {
            keyPressed = true;

            boolean ctrlPressed = GLFW.glfwGetKey(Minecraft.getInstance().getWindow().handle(),
                    GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;

            // ctrlキーが押されていた場合
            if (ctrlPressed) {
                // 進捗確認
                if (!ClientAdvancementUtil.hasUnlockedSpacetimeAdvancement(Minecraft.getInstance())) return false;

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
    private static void getRangeEntities(Minecraft minecraft, Player player, List<Entity> rangeEntities) {
        Level level = minecraft.level;
        if (level == null) return;

        AABB searchBox = player.getBoundingBox().inflate(30);
        for (Entity entity : level.getEntities(player, searchBox)) {
            if (entity instanceof PortalEntity) continue;                   // 特定のエンティティ判定
            if (hasObstacleInSight(minecraft, entity, player)) continue;    // ブロックの判定
            rangeEntities.add(entity);
        }
    }

    /**
     * プレイヤーとターゲットの間に遮るブロックがあるかを判定
     * 草など一部のブロックは除外されてる
     */
    private static boolean hasObstacleInSight(Minecraft minecraft, Entity targetEntity, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = targetEntity.getEyePosition();

        // プレイヤー視点からターゲットまでの直線上にある最初のブロックを取得
        BlockHitResult hit = minecraft.level.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        if (hit.getType() != HitResult.Type.BLOCK) return false;

        BlockState blockState = minecraft.level.getBlockState(hit.getBlockPos());
        Identifier id = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());

        // 通過可能なブロックを指定
        return !blockState.is(ModTags.Blocks.PASS_THROUGH_BLOCK)
                && !id.getPath().contains("vine")
                && !id.getPath().contains("bush");
    }

    /**
     * 視線上で最も近いエンティティを選択
     */
    private static Entity selectEntity(Player player, List<Entity> rangeEntities) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0F).scale(30.0));

        Entity closestEntity = null;
        double closestDistanceSqr = 900;    // 最大30ブロック

        // 矢を一時的に貯めるリスト
        List<Entity> arrowCandidates = new ArrayList<>();

        // ---1回目の判定---
        for (Entity entity : rangeEntities) {
            if (entity == player) continue;

            // 矢は後回し
            if (entity.is(EntityTypeTags.IMPACT_PROJECTILES)) {
                arrowCandidates.add(entity);
                continue;
            }

            Optional<Vec3> hitPos = entity.getBoundingBox().clip(start, end);
            if (hitPos.isPresent()) {
                double distanceSqr = start.distanceToSqr(hitPos.get());
                if (distanceSqr < closestDistanceSqr) {
                    closestDistanceSqr = distanceSqr;
                    closestEntity = entity;
                }
            }
        }

        // ---2回目の判定（矢用）---
        if (closestEntity == null) {
            for (Entity arrow : arrowCandidates) {
                Optional<Vec3> hitPos = arrow.getBoundingBox().inflate(1.5).clip(start, end);   // 矢だけ広めにする
                if (hitPos.isPresent()) {
                    double distanceSqr = start.distanceToSqr(hitPos.get());
                    if (distanceSqr < closestDistanceSqr) {
                        closestDistanceSqr = distanceSqr;
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
    private static Entity divideRangeEntities(Player player, List<Entity> rangeEntities) {
        // プレイヤーが乗ってるエンティティ
        Entity playerVehicle = player.getVehicle();

        Entity midCandidate = null;
        int midCount = 0;

        Entity fallbackCandidate = null;
        int fallbackCount = 0;

        for (Entity entity : rangeEntities) {
            // プレイヤー自身とプレイヤーの乗ってるエンティティは除外
            if (entity instanceof Player || entity == playerVehicle) continue;

            double d = player.distanceToSqr(entity);

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