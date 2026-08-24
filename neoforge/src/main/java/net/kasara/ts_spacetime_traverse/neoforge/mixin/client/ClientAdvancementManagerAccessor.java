package net.kasara.ts_spacetime_traverse.neoforge.mixin.client;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * ClientAdvancementsにアクセスするためのMixinAccessor
 * 通常は非公開のprogressフィールドに外部から安全にアクセスするためのインターフェース
 */
@Mixin(ClientAdvancements.class)
public interface ClientAdvancementManagerAccessor {

    /**
     * クライアントが保持している全アドバンスメントの進捗情報を取得する
     */
    @Accessor("progress")
    Map<AdvancementHolder, AdvancementProgress> getProgress();
}
