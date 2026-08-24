package net.kasara.ts_spacetime_traverse.neoforge.entity;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.kasara.ts_spacetime_traverse.neoforge.TSSpacetimeTraverse;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister.Entities ENTITIES =
            DeferredRegister.createEntities(TokorotenSlimeAPI.getModId());

    // ポータルエンティティのEntityType定義
    public static final DeferredHolder<EntityType<?>, EntityType<PortalEntity>> PORTAL =
            ENTITIES.registerEntityType(
                    "portal",
                    PortalEntity::new,
                    MobCategory.MISC,
                    builder -> builder
                            .noLootTable()                          // 死亡時にドロップなし
                            .sized(1.0F, 2.0F)          // ヒットボックスの幅・高さ
                            .clientTrackingRange(8)   // サーバーとの同期範囲
                            .updateInterval(20)                     // 同期間隔
            );

    /**
     * エンティティ登録処理を初期化時に呼び出す
     */
    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);

        TSSpacetimeTraverse.LOGGER.info("Registering addon Entities for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
