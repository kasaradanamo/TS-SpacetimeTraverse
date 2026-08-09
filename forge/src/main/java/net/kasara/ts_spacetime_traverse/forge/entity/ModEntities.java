package net.kasara.ts_spacetime_traverse.forge.entity;

import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.entity.ModEntitiesCommon;
import net.kasara.ts_spacetime_traverse.entity.PortalEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TokorotenSlimeCommon.MOD_ID);

    // ポータルエンティティのEntityType定義
    public static final RegistryObject<EntityType<PortalEntity>> PORTAL =
            ENTITIES.register("portal", () -> EntityType.Builder.<PortalEntity>of(PortalEntity::new, MobCategory.MISC)
                    .sized(1.0F, 2.0F)          // ヒットボックスの幅・高さ
                    .clientTrackingRange(8)   // サーバーとの同期範囲
                    .updateInterval(20)                     // 同期間隔
                    .build(TokorotenSlimeCommon.MOD_ID + ":portal")
            );

    /**
     * エンティティ登録処理を初期化時に呼び出す
     */
    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);

        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Entities for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }

    /**
     * commonホルダーへ登録済みインスタンスを反映する(FMLCommonSetupEventから呼び出す)
     */
    public static void initCommonHolder() {
        ModEntitiesCommon.PORTAL = PORTAL.get();
    }
}
