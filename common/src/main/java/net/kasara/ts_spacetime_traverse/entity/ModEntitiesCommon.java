package net.kasara.ts_spacetime_traverse.entity;

import net.minecraft.world.entity.EntityType;

/**
 * ローダー側で登録されたEntityTypeをcommonから参照するための保持クラス
 */
public class ModEntitiesCommon {

    public static EntityType<PortalEntity> PORTAL;

    private ModEntitiesCommon() {}
}
