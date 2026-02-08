package net.kasara.ts_spacetime_traverse.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kasara.ts_spacetime_traverse.client.data.WaypointClientCache;
import net.kasara.ts_spacetime_traverse.client.gui.screen.PortalActionScreen;
import net.kasara.ts_spacetime_traverse.client.gui.screen.WaypointFormScreen;
import net.kasara.ts_spacetime_traverse.client.gui.widget.WaypointListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/**
 * PortalActionScreenで発生するボタン押下などのイベントを処理するコントローラ
 *
 * @param screen 操作対象のScreen
 */
@Environment(EnvType.CLIENT)
public record PortalActionController(PortalActionScreen screen) {

    /**
     * 選択ウェイポイント先のポータルを設置
     *
     * @param selected 現在選択されているリストエントリ
     */
    public void onPlacePortal(@Nullable WaypointListWidget.Entry selected) {
        if (selected == null) return;
        screen.close(); // 画面を閉じる
        PortalActionClientHandler.placePortal(MinecraftClient.getInstance().player, selected.data.uuid());
    }

    /**
     * ウェイポイント登録画面を開く
     */
    public void onOpenWaypointFormRegister() {
        MinecraftClient.getInstance().setScreen(WaypointFormScreen.register(screen));
    }

    /**
     * 選択ウェイポイントをクイック登録
     *
     * @param selected 選択エントリ
     */
    public void onQuickRegister(@Nullable WaypointListWidget.Entry selected) {
        if (selected == null) return;
        WaypointClientManager.registerQuick(selected.data.uuid());
        screen.updateActionButtons(selected);   // ボタン状態更新
    }

    /**
     * 選択ウェイポイントを編集
     */
    public void onEditWaypoint(@Nullable WaypointListWidget.Entry selected) {
        if (selected == null) return;
        MinecraftClient.getInstance().setScreen(WaypointFormScreen.edit(screen, selected.data));
    }

    /**
     * 選択ウェイポイントを削除
     *
     * @param selected 選択エントリ
     */
    public void onDeleteWaypoint(@Nullable WaypointListWidget.Entry selected) {
        if (selected == null) return;

        // 確認画面を表示して、削除を実行
        MinecraftClient.getInstance().setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        WaypointClientManager.applyWaypointChange(selected.data, true);
                        screen.refreshWaypointList();   // リストを更新
                    }
                    MinecraftClient.getInstance().setScreen(screen);    // 元の画面に戻す
                },
                Text.translatable("screen.tokorotenslime.portal_action.delete.message"),
                Text.literal("'" + selected.data.name() + "'"),
                Text.translatable("screen.tokorotenslime.portal_action.delete"),
                Text.translatable("screen.tokorotenslime.portal_action.delete.cancel")
        ) {
            @Override
            public boolean shouldPause() {
                return false;   // ゲームを一時停止させない
            }
        });
    }

    /**
     * 各ボタンの状態(有効/無効)を更新
     *
     * @param selected 選択されているリストエントリ
     * @param place    ポータル設置ボタン
     * @param quick    クイック登録ボタン
     * @param edit     編集ボタン
     * @param delete   削除ボタン
     * @param register ウェイポイント登録ボタン
     */
    public void updateButtons(
            @Nullable WaypointListWidget.Entry selected,
            ButtonWidget place,
            ButtonWidget quick,
            ButtonWidget edit,
            ButtonWidget delete,
            ButtonWidget register
    ) {
        boolean hasSelection = selected != null;
        boolean isQuick = hasSelection && selected.data.uuid().equals(WaypointClientCache.getQuick());

        place.active = hasSelection;
        quick.active = hasSelection && !isQuick;
        edit.active = hasSelection;
        delete.active = hasSelection;
        register.active = canRegisterWaypoint();
    }

    /**
     * ウェイポイントを登録可能か判定
     *
     * @return 登録されてるのが10個未満ならtrue
     */
    private boolean canRegisterWaypoint() {
        return WaypointClientCache.getAll().size() < 10;
    }
}
