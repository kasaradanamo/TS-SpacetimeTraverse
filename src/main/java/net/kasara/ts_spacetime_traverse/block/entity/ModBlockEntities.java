package net.kasara.ts_spacetime_traverse.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * MODで使用するBlockEntityタイプの管理クラス
 */
public class ModBlockEntities {

    /** VoidBlock用のBlockEntity */
    public static BlockEntityType<VoidBlockEntity> VOID_BE;

    /**
     * BlockEntityの登録
     */
    public static void registerBlockEntities() {

        // VoidBlockに対応するBlockEntityタイプを生成
        VOID_BE = FabricBlockEntityTypeBuilder.create(VoidBlockEntity::new, ModBlocks.VOID_BLOCK).build();

        // レジストリに登録
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(TokorotenSlimeAPI.getModId(), "void_be"), VOID_BE);

        // ログ出力
        TSSpacetimeTraverse.LOGGER.info("Registering addon Block Entities for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
