package net.kasara.ts_spacetime_traverse.entity;

import net.minecraft.world.entity.EntityType;

/**
 * ローダー固有のModEntitiesが初期化時にセットする、登録済みEntityTypeの共有ホルダー。
 */
public final class ModEntitiesCommon {

    public static EntityType<PortalEntity> PORTAL;

    private ModEntitiesCommon() {}
}
