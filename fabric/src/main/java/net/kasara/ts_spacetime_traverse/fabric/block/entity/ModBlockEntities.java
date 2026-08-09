package net.kasara.ts_spacetime_traverse.fabric.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.block.entity.ModBlockEntitiesCommon;
import net.kasara.ts_spacetime_traverse.block.entity.VoidBlockEntity;
import net.kasara.ts_spacetime_traverse.fabric.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    // VoidBlock用のBlockEntity
    public static final BlockEntityType<VoidBlockEntity> VOID_BE =
            registerBlockEntities("void_be", VoidBlockEntity::new, ModBlocks.VOID_BLOCK);

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntities(String name, FabricBlockEntityTypeBuilder.Factory<T> factory, Block... blocks) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                new ResourceLocation(TokorotenSlimeCommon.MOD_ID, name),
                FabricBlockEntityTypeBuilder.create(factory, blocks).build()
        );
    }

    /**
     * ModBlockEntitiesの登録処理を呼び出す
     */
    public static void register() {
        ModBlockEntitiesCommon.VOID_BE = VOID_BE;

        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Block Entities for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }
}
