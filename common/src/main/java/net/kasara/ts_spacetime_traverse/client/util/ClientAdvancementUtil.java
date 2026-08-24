package net.kasara.ts_spacetime_traverse.client.util;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.Function;

public class ClientAdvancementUtil {

    // 使用可能かどうかを判定するための実績ID
    private static final Identifier SPACETIME_ADVANCEMENT =
            Identifier.fromNamespaceAndPath(TokorotenSlimeAPI.getModId(), "use_spacetime_eye");

    // ローダー側が代入する、進捗マップ取得用のブリッジ
    public static Function<ClientAdvancements, Map<AdvancementHolder, AdvancementProgress>> GET_PROGRESS_MAP;

    /**
     * クライアント側の実績情報を使って、特定の実績が解除済みかどうか判定
     *
     * @return 実績が解除されてればtrue、されてなければfalse
     */
    public static boolean hasUnlockedSpacetimeAdvancement(Minecraft minecraft) {
        if (minecraft == null || minecraft.player == null || minecraft.getConnection() == null) return false;

        var clientAdvancementManager = minecraft.getConnection().getAdvancements();

        // ブリッジ経由で進捗マップを取得
        Map<AdvancementHolder, AdvancementProgress> progresses =
                GET_PROGRESS_MAP.apply(clientAdvancementManager);

        AdvancementHolder holder = clientAdvancementManager.get(SPACETIME_ADVANCEMENT);
        if (holder == null) return false;

        AdvancementProgress progress = progresses.get(holder);
        return progress != null && progress.isDone();
    }

    private ClientAdvancementUtil() {}
}