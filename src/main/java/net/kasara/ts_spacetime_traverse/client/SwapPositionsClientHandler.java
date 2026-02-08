package net.kasara.ts_spacetime_traverse.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.TargetEntityIdC2SPacket;
import net.kasara.ts_spacetime_traverse.client.option.ModKeyBindings;
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

import java.util.*;

/**
 * クライアント側でスワップ操作に関するキー入力とアクション処理をするハンドラ
 */
@Environment(EnvType.CLIENT)
public class SwapPositionsClientHandler {

    // モード切替キーが押下中かどうか（押下判定の連打防止用）
    private static boolean keyPressed = false;

    public static void handleSwapPositions(MinecraftClient client) {
        List<Entity> rangeEntities = new ArrayList<>();
        boolean random = false;
        PlayerEntity player = client.player;

        if (player == null) return;

        if (!pushKey()) return;

        // 周りのエンティティを取得
        getRangeEntities(client, rangeEntities);

        // 視線に最も近いエンティティを取得
        Entity target = selectEntity(player, rangeEntities);
        if (target == null) return;

        // 同じエンティティを二度と選ばないように除外
        rangeEntities.remove(target);

        // 4ブロック以内だったら、他の候補から交換相手を再選択
        if (player.distanceTo(target) <= 4 && !rangeEntities.isEmpty()) {
            target = divideRangeEntities(player, rangeEntities);
            random = true;
        }

        // 選択したエンティティIDとエンティティのいるディメンション名とランダムbooleanをサーバー送信して入れ替え処理
        TargetEntityIdC2SPacket.send(target.getId(), target.getEntityWorld().getRegistryKey().getValue().toString(), random);
    }

    /**
     * キーの押し始めのみtrue
     */
    private static boolean pushKey() {
        // 現在のフレームでキーが押されているかどうかを取得
        boolean isPressed = ModKeyBindings.SWAP_POSITIONS.isPressed();

        if (isPressed && !keyPressed) {
            keyPressed = true;
            return true;
        }
        else if (!isPressed) keyPressed = false;

        return false;
    }

    /**
     * プレイヤーの周囲30ブロック以内で、視線が通ってるエンティティをフィルタし、
     * 草などの視線を防げないブロックは無視される
     */
    private static void getRangeEntities(MinecraftClient client, List<Entity> rangeEntities) {
        World world = client.world;
        PlayerEntity player = client.player;
        if (world == null || player == null) return;

        // 周囲のエンティティを取得して、視線が通ってるものだけをフィルタ
        world.getOtherEntities(player, player.getBoundingBox().expand(30.0))
                .stream()
                .filter(e -> !(e instanceof PortalEntity))
                .filter(e -> !hasObstacleInSight(client, e, player)) // 無視されるブロックの判定
                .forEach(rangeEntities::add);
    }

    /**
     * プレイヤーとターゲットの間に遮るブロックがあるかを判定
     * 草など一部のブロックは除外されてる
     */
    private static boolean hasObstacleInSight(MinecraftClient client, Entity targetEntity, PlayerEntity player) {
        Vec3d start = player.getEyePos();
        Vec3d end = targetEntity.getEyePos();

        // プレイヤー視点からターゲットまでの直線上にある最初のブロックを取得
        BlockHitResult hit = Objects.requireNonNull(client.world).raycast(new RaycastContext(
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
        double closestDistance = 30.0;

        // 矢を一時的に貯めるリスト
        List<Entity> arrowCandidates = new ArrayList<>();

        // ---1回目の判定---
        for (Entity entity : rangeEntities) {
            // プレイヤー自身は除外
            if (entity == player) continue;

            boolean isProjectile = entity.getType().isIn(EntityTypeTags.IMPACT_PROJECTILES);

            // 矢は後回し
            if (isProjectile) {
                arrowCandidates.add(entity);
                continue;
            }

            Box expandedBox = entity.getBoundingBox().expand(0);
            Optional<Vec3d> hitPos = expandedBox.raycast(start, end);

            if(hitPos.isPresent()) {
                double distance = start.distanceTo(hitPos.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }

        // ---2回目の判定（矢用）---
        if (closestEntity == null && !arrowCandidates.isEmpty()) {
            for (Entity arrow : arrowCandidates) {
                Box box = arrow.getBoundingBox().expand(1.5F);   // 矢だけ広めにする
                Optional<Vec3d> hitPos = box.raycast(start, end);

                if (hitPos.isPresent()) {
                    double distance = start.distanceTo(hitPos.get());
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestEntity = arrow;
                    }
                }
            }
        }

        return closestEntity;
    }

    /**
     * 距離で分類し、10～20ブロックの中距離帯があればその中からランダム、それ以外は全体からランダム選択
     */
    private static Entity divideRangeEntities(PlayerEntity player, List<Entity> rangeEntities) {
        List<Entity> sorted = rangeEntities.stream()
                .sorted(Comparator.comparingDouble(player::distanceTo))
                .toList();

        List<Entity> midRange = sorted.stream()
                .filter(e -> {
                    double d = player.distanceTo(e);
                    return d > 10 && d <= 20;
                }).toList();

        return !midRange.isEmpty() ? randomSelect(midRange) : randomSelect(sorted);
    }

    /**
     * リストからランダムに1体選ぶ。ただし、プレイヤーは除外（他プレイヤーを誤って選ばないように）
     */
    private static Entity randomSelect(List<Entity> entities) {
        Random rand = new Random();
        Entity sel;
        do {
            sel = entities.get(rand.nextInt(entities.size()));
        } while (sel instanceof PlayerEntity);
        return sel;
    }
}
