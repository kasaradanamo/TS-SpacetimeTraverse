package net.kasara.ts_spacetime_traverse.fabric.entity;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.kasara.ts_spacetime_traverse.fabric.TSSpacetimeTraverse;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    // ポータルエンティティのEntityType定義
    public static final EntityType<PortalEntity> PORTAL = registerEntity(
            "portal",
            EntityType.Builder.<PortalEntity>of(PortalEntity::new, MobCategory.MISC)
                    .sized(1.0F, 2.0F)          // ヒットボックスの幅・高さ
                    .clientTrackingRange(8)   // サーバーとの同期範囲
                    .updateInterval(20)                     // 同期間隔
    );

    private static <T extends Entity> EntityType<T> registerEntity(String name, EntityType.Builder<T> builder) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TokorotenSlimeAPI.getModId(), name);
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                id,
                builder.build(id.toString())
        );
    }

    /**
     * エンティティ登録処理を初期化時に呼び出す
     */
    public static void register() {
        TSSpacetimeTraverse.LOGGER.info("Registering addon Entities for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
