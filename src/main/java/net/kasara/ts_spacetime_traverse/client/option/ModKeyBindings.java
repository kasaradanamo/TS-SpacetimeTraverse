package net.kasara.ts_spacetime_traverse.client.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * キーバインディング登録クラス
 */
@Environment(EnvType.CLIENT)
public class ModKeyBindings {

    // モード切替用のキー
    public static KeyBinding POSITION_SWAP;
    // ポータル出現
    public static KeyBinding PORTAL_ACTION;

    /**
     * 登録＆ログ出力
     */
    public static void register() {
        POSITION_SWAP = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tokorotenslime.position_swap",   // キー名（翻訳用）
                InputUtil.Type.KEYSYM,                      // キーの種類
                GLFW.GLFW_KEY_R,                            // デフォルトのキー
                TokorotenSlimeAPI.getKeyBindingCategory()   // カテゴリ名（オプション画面で表示されるグループ）
        ));

        PORTAL_ACTION = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tokorotenslime.portal_action",   // キー名（翻訳用）
                InputUtil.Type.KEYSYM,                      // キーの種類
                GLFW.GLFW_KEY_C,                            // デフォルトのキー
                TokorotenSlimeAPI.getKeyBindingCategory()   // カテゴリ名（オプション画面で表示されるグループ）
        ));

        // 登録完了ログを出力
        TSSpacetimeTraverse.LOGGER.info("Registering addon Key Bindings for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }
}
