package net.kasara.ts_spacetime_traverse.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * VoidBlockに紐づくBlockEntity
 */
public class VoidBlockEntity extends BlockEntity {

    // 空状態が続いたtick数
    private int emptyTicks = 0;

    public VoidBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntitiesCommon.VOID_BE, pos, state);
    }

    /**
     * サーバー側Tick処理
     *
     * @param level レベル
     * @param pos ブロック位置
     * @param state ブロック状態
     * @param be VoidBlockEntity
     */
    public static void tick(Level level, BlockPos pos, BlockState state, VoidBlockEntity be) {
        // クライアント側では処理しない
        if (level.isClientSide()) return;

        // ブロック上の1x1x1の領域を監視
        AABB box = new AABB(
                pos.getX(), pos.getY() + 1, pos.getZ(),
                pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1
        );

        // 領域内にEntityがいるか確認
        boolean hasEntity = !level.getEntitiesOfClass(Entity.class, box).isEmpty();

        if (hasEntity) {
            // Entityがいる場合はカウントリセット
            be.emptyTicks = 0;
        } else {
            // Entityがいない場合はカウント増加
            be.emptyTicks++;
        }

        // 空状態が40tick以上続いたらブロック破壊
        if (be.emptyTicks >= 40) {
            level.destroyBlock(pos, false);
        }
    }
}