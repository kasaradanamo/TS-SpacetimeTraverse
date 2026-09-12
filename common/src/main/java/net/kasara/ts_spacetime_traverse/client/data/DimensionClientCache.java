package net.kasara.ts_spacetime_traverse.client.data;

import net.kasara.ts_spacetime_traverse.util.DimensionBounds;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * サーバーから送信されたディメンション一覧をクライアント側で一時保持するキャッシュ
 */
public final class DimensionClientCache {

    private static Map<ResourceLocation, DimensionBounds> dimensions = Map.of();

    /**
     * ディメンション一覧を全上書きする(join時に)
     */
    public static void setAll(Map<ResourceLocation, DimensionBounds> newDimensions) {
        dimensions = Map.copyOf(newDimensions);
    }

    /**
     * 指定ディメンションが存在するか確認
     */
    public static boolean contains(ResourceLocation id) {
        return dimensions.containsKey(id);
    }

    /**
     * データ取得
     */
    public static DimensionBounds get(ResourceLocation id) {
        return dimensions.get(id);
    }

    private DimensionClientCache() {}
}
