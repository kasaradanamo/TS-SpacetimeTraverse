package net.kasara.ts_spacetime_traverse.neoforge.client.option;

import com.mojang.blaze3d.platform.InputConstants;
import net.kasara.tokorotenslime.api.TokorotenSlimeClientAPI;
import net.minecraft.client.KeyMapping;

public class ModKeyMappings {

    // モード切替用のキー
    public static final KeyMapping POSITION_SWAP = new KeyMapping(
            "key.tokorotenslime.position_swap",  // キー名（翻訳用）
            InputConstants.Type.KEYBOARD,               // キーの種類
            InputConstants.KEY_R,                      // デフォルトのキー
            TokorotenSlimeClientAPI.getKeyMappingCategory()  // カテゴリ名（オプション画面で表示されるグループ）
    );

    // ポータル出現
    public static final KeyMapping PORTAL_ACTION = new KeyMapping(
            "key.tokorotenslime.portal_action",   // キー名（翻訳用）
            InputConstants.Type.KEYBOARD,                // キーの種類
            InputConstants.KEY_C,                       // デフォルトのキー
            TokorotenSlimeClientAPI.getKeyMappingCategory()   // カテゴリ名（オプション画面で表示されるグループ）
    );
}
