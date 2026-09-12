package net.kasara.ts_spacetime_traverse.neoforge.block.entity;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.block.entity.VoidBlockEntity;
import net.kasara.ts_spacetime_traverse.neoforge.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.neoforge.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TokorotenSlimeAPI.getModId());

    // VoidBlock用BlockEntityType
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VoidBlockEntity>> VOID_BE =
            BLOCK_ENTITIES.register(
                    "void_be",
                    () -> BlockEntityType.Builder.of(VoidBlockEntity::new, ModBlocks.VOID_BLOCK.get()).build(null)
            );

    /**
     * ModBlockEntitiesの登録処理を呼び出す
     */
    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);

        TSSpacetimeTraverse.LOGGER.info("Registering addon Block Entities for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
