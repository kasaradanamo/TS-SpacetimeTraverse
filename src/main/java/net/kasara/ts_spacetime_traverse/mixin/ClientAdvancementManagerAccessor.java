package net.kasara.ts_spacetime_traverse.mixin;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.network.ClientAdvancementManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * ClientAdvancementManager にアクセスするための Mixin Accessor
 * 通常は非公開である advancementProgresses フィールドに外部から安全にアクセスするためのインターフェース
 */
@Mixin(ClientAdvancementManager.class)
public interface ClientAdvancementManagerAccessor {

    /**
     * クライアントが保持している全アドバンスメントの進捗情報を取得する
     */
    @Accessor("advancementProgresses")
    Map<AdvancementEntry, AdvancementProgress> getAdvancementProgresses();
}
