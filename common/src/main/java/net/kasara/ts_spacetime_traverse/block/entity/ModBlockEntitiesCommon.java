package net.kasara.ts_spacetime_traverse.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * ローダー側で登録されたBlockEntityTypeをcommonから参照するための保持クラス
 */
public class ModBlockEntitiesCommon {

    public static BlockEntityType<VoidBlockEntity> VOID_BE;

    private ModBlockEntitiesCommon() {}
}
