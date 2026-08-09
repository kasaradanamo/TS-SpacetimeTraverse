package net.kasara.ts_spacetime_traverse.client.util;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Function;

public class ClientAdvancementUtil {

    // 使用可能かどうかを判定するための実績ID
    private static final ResourceLocation SPACETIME_ADVANCEMENT =
            new ResourceLocation(TokorotenSlimeAPI.getModId(), "use_spacetime_eye");

    /**
     * ClientAdvancements#progress(private)を取り出す関数
     */
    public static Function<ClientAdvancements, Map<Advancement, AdvancementProgress>> GET_PROGRESS_MAP;

    /**
     * クライアント側の実績情報を使って、特定の実績が解除済みかどうか判定
     *
     * @return 実績が解除されてればtrue、されてなければfalse
     */
    public static boolean hasUnlockedSpacetimeAdvancement(Minecraft minecraft) {
        if (minecraft == null || minecraft.player == null || minecraft.getConnection() == null) return false;

        ClientAdvancements clientAdvancementManager = minecraft.getConnection().getAdvancements();

        // ローダー固有の実装(Mixin経由)で進捗マップを取得
        Map<Advancement, AdvancementProgress> progresses = GET_PROGRESS_MAP.apply(clientAdvancementManager);

        Advancement advancement = clientAdvancementManager.getAdvancements().get(SPACETIME_ADVANCEMENT);
        if (advancement == null) return false;

        AdvancementProgress progress = progresses.get(advancement);
        return progress != null && progress.isDone();
    }

    private ClientAdvancementUtil() {}
}
