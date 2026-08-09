package net.kasara.ts_spacetime_traverse.block;

import net.kasara.ts_spacetime_traverse.block.entity.VoidBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * ポータルくぐった際足元に何もなかった時に出てくる一時ブロック
 */
public class VoidBlock extends HalfTransparentBlock implements EntityBlock {

    public VoidBlock(Properties pros) {
        super(pros);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VoidBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // クライアント側では処理不要
        return level.isClientSide() ? null : (l, p, s, be) -> {
            if (be instanceof VoidBlockEntity voidBe) {
                // サーバー側でVoidBlockEntityのTickを呼び出す
                VoidBlockEntity.tick(l, p, s, voidBe);
            }
        };
    }
}
