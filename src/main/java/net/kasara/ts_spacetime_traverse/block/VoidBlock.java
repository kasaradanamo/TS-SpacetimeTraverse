package net.kasara.ts_spacetime_traverse.block;

import net.kasara.ts_spacetime_traverse.block.entity.VoidBlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.TransparentBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * ポータルくぐった際足元何もなかった時に出てくる一時ブロック
 */
public class VoidBlock extends TransparentBlock implements BlockEntityProvider {
    public VoidBlock(Settings settings) {
        super(settings);
    }

    /**
     * ブロックエンティティの生成
     */
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VoidBlockEntity(pos, state);
    }

    /**
     * サーバー側でのTick処理取得
     */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        // クライアント側では処理不要
        return world.isClient() ? null : (w, p, s, be) -> {
            if (be instanceof VoidBlockEntity voidBe) {
                // サーバー側でVoidBlockEntityのTickを呼び出す
                VoidBlockEntity.tick(w, p, s, voidBe);
            }
        };
    }
}
