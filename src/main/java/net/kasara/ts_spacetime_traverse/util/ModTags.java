package net.kasara.ts_spacetime_traverse.util;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {

    public static class Blocks {

        /** すり抜けれるブロック */
        public static final TagKey<Block> PASS_THROUGH_BLOCK = createTag("pass_through_block");

        /**
         * タグを作成するヘルパーメソッド
         * @param name タグ名
         * @return Block用のTagKey
         */
        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(TokorotenSlimeAPI.getModId(), name));
        }
    }
}
