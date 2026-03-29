package net.kasara.ts_spacetime_traverse.client.util;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.mixin.ClientAdvancementManagerAccessor;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ClientAdvancementUtil {

    // 使用可能かどうかを判定するための実績ID
    private static final Identifier SPACETIME_ADVANCEMENT =
            Identifier.of(TokorotenSlimeAPI.getModId(), "use_spacetime_eye");

    /**
     * クライアント側の実績情報を使って、特定の実績が解除済みかどうか判定
     *
     * @return 実績が解除されてればtrue、されてなければfalse
     */
    public static boolean hasUnlockedSpacetimeAdvancement(MinecraftClient client) {
        if (client == null || client.player == null || client.getNetworkHandler() == null) return false;

        var clientAdvancementManager = client.getNetworkHandler().getAdvancementHandler();

        // mixin経由で進捗マップを取得
        Map<AdvancementEntry, AdvancementProgress> progresses =
                ((ClientAdvancementManagerAccessor) clientAdvancementManager).getAdvancementProgresses();

        AdvancementEntry entry = clientAdvancementManager.get(SPACETIME_ADVANCEMENT);
        if (entry == null) return false;

        AdvancementProgress progress = progresses.get(entry);
        return progress != null && progress.isDone();
    }

    private ClientAdvancementUtil() {}
}
