package net.kasara.ts_spacetime_traverse.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    // VoidBlock用のBlockEntity
    public static BlockEntityType<VoidBlockEntity> VOID_BE;

    /**
     * BlockEntityの登録
     */
    public static void register() {

        // VoidBlockに対応するBlockEntityタイプを生成
        VOID_BE = FabricBlockEntityTypeBuilder.create(VoidBlockEntity::new, ModBlocks.VOID_BLOCK).build();

        // レジストリに登録
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(TokorotenSlimeAPI.getModId(), "void_be"), VOID_BE);

        // ログ出力
        TSSpacetimeTraverse.LOGGER.info("Registering addon Block Entities for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}