package net.kasara.ts_spacetime_traverse.client.util;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.mixin.ClientAdvancementManagerAccessor;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class ClientAdvancementUtil {

    // 使用可能かどうかを判定するための実績ID
    private static final Identifier SPACETIME_ADVANCEMENT =
            Identifier.fromNamespaceAndPath(TokorotenSlimeAPI.getModId(), "use_spacetime_eye");

    /**
     * クライアント側の実績情報を使って、特定の実績が解除済みかどうか判定
     *
     * @return 実績が解除されてればtrue、されてなければfalse
     */
    public static boolean hasUnlockedSpacetimeAdvancement(Minecraft minecraft) {
        if (minecraft == null || minecraft.player == null || minecraft.getConnection() == null) return false;

        var clientAdvancementManager = minecraft.getConnection().getAdvancements();

        // mixin経由で進捗マップを取得
        Map<AdvancementHolder, AdvancementProgress> progresses =
                ((ClientAdvancementManagerAccessor) clientAdvancementManager).getProgress();

        AdvancementHolder holder = clientAdvancementManager.get(SPACETIME_ADVANCEMENT);
        if (holder == null) return false;

        AdvancementProgress progress = progresses.get(holder);
        return progress != null && progress.isDone();
    }

    private ClientAdvancementUtil() {}
}