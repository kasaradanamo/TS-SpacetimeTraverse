package net.kasara.ts_spacetime_traverse.entity;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

/**
 * EntityType の登録クラス
 */
public class ModEntities {

    /** PortalEntity用のRegistryKey */
    public static final RegistryKey<EntityType<?>> key = RegistryKey.of(Registries.ENTITY_TYPE.getKey(),
            Identifier.of(TokorotenSlimeAPI.getModId(), "portal"));


    /** ポータルエンティティのEntityType定義 */
    public static final EntityType<PortalEntity> PORTAL = Registry.register(
            Registries.ENTITY_TYPE,
            key.getValue(),
            EntityType.Builder.<PortalEntity>create(PortalEntity::new, SpawnGroup.MISC)
                    .dropsNothing()                     // 死亡時にドロップなし
                    .dimensions(1.0F, 2.0F) // ヒットボックスの幅・高さ
                    .maxTrackingRange(8)                // サーバーとの同期範囲
                    .trackingTickInterval(20)           // 同期間隔
                    .build(key)
    );

    /**
     * エンティティ登録処理を初期化時に呼び出す
     */
    public static void registerModEntities() {
        TSSpacetimeTraverse.LOGGER.info("Registering addon Entities for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
