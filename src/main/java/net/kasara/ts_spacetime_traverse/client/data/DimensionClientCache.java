package net.kasara.ts_spacetime_traverse.client.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import java.util.Set;

/**
 * サーバーから送信されたディメンション一覧を
 * クライアント側で一時保持するキャッシュ
 */
@Environment(EnvType.CLIENT)
public final class DimensionClientCache {

    private static Set<Identifier> dimensions = Set.of();

    private DimensionClientCache() {}

    /**
     * ディメンション一覧を全上書きする(join時に)
     */
    public static void setAll(Set<Identifier> newDimensions) {
        dimensions = Set.copyOf(newDimensions);
    }

    /**
     * 指定ディメンションが存在するか確認
     */
    public static boolean contains(Identifier dimensionId) {
        return dimensions.contains(dimensionId);
    }
}
