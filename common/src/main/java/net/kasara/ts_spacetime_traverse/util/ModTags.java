package net.kasara.ts_spacetime_traverse.util;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static class Blocks {

        // すり抜けれるブロック
        public static final TagKey<Block> PASS_THROUGH_BLOCK = createTag("pass_through_block");

        private static TagKey<Block> createTag(String name) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TokorotenSlimeAPI.getModId(), name));
        }
    }
}
