package net.kasara.ts_spacetime_traverse.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * ローダー固有のModBlockEntitiesが初期化時にセットする、登録済みBlockEntityTypeの共有ホルダー。
 */
public final class ModBlockEntitiesCommon {

    public static BlockEntityType<VoidBlockEntity> VOID_BE;

    private ModBlockEntitiesCommon() {}
}
