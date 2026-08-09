package net.kasara.ts_spacetime_traverse.forge.block.entity;

import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.block.entity.ModBlockEntitiesCommon;
import net.kasara.ts_spacetime_traverse.block.entity.VoidBlockEntity;
import net.kasara.ts_spacetime_traverse.forge.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TokorotenSlimeCommon.MOD_ID);

    // VoidBlock用のBlockEntity
    public static final RegistryObject<BlockEntityType<VoidBlockEntity>> VOID_BE =
            BLOCK_ENTITIES.register("void_be",
                    () -> BlockEntityType.Builder.of(VoidBlockEntity::new, ModBlocks.VOID_BLOCK.get()).build(null));

    /**
     * BlockEntityの登録
     */
    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);

        // ログ出力
        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Block Entities for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }

    /**
     * commonホルダーへ登録済みインスタンスを反映する(FMLCommonSetupEventから呼び出す)
     */
    public static void initCommonHolder() {
        ModBlockEntitiesCommon.VOID_BE = VOID_BE.get();
    }
}
