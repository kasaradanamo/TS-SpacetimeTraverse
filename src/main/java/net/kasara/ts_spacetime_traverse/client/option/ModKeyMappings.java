package net.kasara.ts_spacetime_traverse.client.option;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * キーマッピング登録クラス
 */
@Environment(EnvType.CLIENT)
public class ModKeyMappings {

    // モード切替用のキー
    public static KeyMapping POSITION_SWAP;
    // ポータル出現
    public static KeyMapping PORTAL_ACTION;

    /**
     * 登録＆ログ出力
     */
    public static void register() {
        POSITION_SWAP = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.tokorotenslime.position_swap",  // キー名（翻訳用）
                InputConstants.Type.KEYSYM,                // キーの種類
                GLFW.GLFW_KEY_R,                           // デフォルトのキー
                TokorotenSlimeAPI.getKeyMappingCategory()  // カテゴリ名（オプション画面で表示されるグループ）
        ));

        PORTAL_ACTION = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.tokorotenslime.portal_action",   // キー名（翻訳用）
                InputConstants.Type.KEYSYM,                 // キーの種類
                GLFW.GLFW_KEY_C,                            // デフォルトのキー
                TokorotenSlimeAPI.getKeyMappingCategory()   // カテゴリ名（オプション画面で表示されるグループ）
        ));

        // 登録完了ログを出力
        TSSpacetimeTraverse.LOGGER.info("Registering addon Key Mappings for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
