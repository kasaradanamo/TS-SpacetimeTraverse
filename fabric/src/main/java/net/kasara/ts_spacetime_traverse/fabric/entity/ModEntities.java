package net.kasara.ts_spacetime_traverse.fabric.entity;

import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.entity.ModEntitiesCommon;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    // ポータルエンティティのEntityType定義
    public static final EntityType<PortalEntity> PORTAL = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(TokorotenSlimeCommon.MOD_ID, "portal"),
            EntityType.Builder.<PortalEntity>of(PortalEntity::new, MobCategory.MISC)
                    .sized(1.0F, 2.0F)          // ヒットボックスの幅・高さ
                    .clientTrackingRange(8)   // サーバーとの同期範囲
                    .updateInterval(20)                     // 同期間隔
                    .build(TokorotenSlimeCommon.MOD_ID + ":portal")
    );

    /**
     * エンティティ登録処理を初期化時に呼び出す
     */
    public static void register() {
        ModEntitiesCommon.PORTAL = PORTAL;

        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Entities for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }
}
