package net.kasara.ts_spacetime_traverse.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kasara.ts_spacetime_traverse.client.PortalActionController;
import net.kasara.ts_spacetime_traverse.client.data.WaypointClientCache;
import net.kasara.ts_spacetime_traverse.client.gui.widget.WaypointListWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/**
 * ポータル操作用のメイン画面
 */
@Environment(EnvType.CLIENT)
public class PortalActionScreen extends Screen {

    /** 3分割レイアウト(ヘッダー/ボディ/フッター) */
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, 8 + 9 + 8 + 4, 60);

    // 中央のウェイポイントリスト
    private WaypointListWidget body;

    /** ボタン群 */
    protected ButtonWidget waypointRegisterButton;
    protected ButtonWidget portalPlaceButton;
    protected ButtonWidget quickRegisterButton;
    protected ButtonWidget editButton;
    protected ButtonWidget deleteButton;

    /** ボタン押下などの処理を担当するコントローラ */
    private final PortalActionController controller;

    public PortalActionScreen() {
        super(Text.translatable("screen.tokorotenslime.portal_action.title"));
        this.controller = new PortalActionController(this);
    }

    @Override
    protected void init() {
        this.initHeader();
        this.initBody();
        this.initFooter();

        // layout内widgetをまとめて追加
        this.layout.forEachChild(this::addDrawableChild);

        // 位置計算
        this.refreshWidgetPositions();
    }

    private void initHeader() {
        this.layout.addHeader(this.title, this.textRenderer);
    }

    private void initBody() {
        this.body = this.layout.addBody(new WaypointListWidget(this.client, this));
    }

    private void initFooter() {
        GridWidget grid = this.layout.addFooter(new GridWidget().setColumnSpacing(8).setRowSpacing(4));
        grid.getMainPositioner().alignHorizontalCenter();
        GridWidget.Adder adder = grid.createAdder(4);

        // 1段目（メイン操作）
        // ポータル設置
        portalPlaceButton = ButtonWidget.builder(
                Text.translatable("screen.tokorotenslime.portal_action.place"),
                button -> controller.onPlacePortal(body.getSelectedOrNull())
        ).build();
        portalPlaceButton.active = false;
        adder.add(portalPlaceButton, 2);

        // ウェイポイント登録
        waypointRegisterButton = ButtonWidget.builder(
                Text.translatable("screen.tokorotenslime.waypoint_register.title"),
                button -> controller.onOpenWaypointFormRegister()
        ).build();
        waypointRegisterButton.active = WaypointClientCache.getAll().size() < 10;
        adder.add(waypointRegisterButton, 2);


        // 2段目（補助）
        // クイック登録
        quickRegisterButton = ButtonWidget.builder(
                Text.translatable("screen.tokorotenslime.portal_action.quick_register"),
                button -> controller.onQuickRegister(body.getSelectedOrNull())
        ).width(71).build();
        quickRegisterButton.active = false;
        adder.add(quickRegisterButton);

        // 編集
        editButton = ButtonWidget.builder(
                Text.translatable("screen.tokorotenslime.portal_action.edit"),
                button -> controller.onEditWaypoint(body.getSelectedOrNull())
        ).width(71).build();
        editButton.active = false;
        adder.add(editButton);

        // 削除
        deleteButton = ButtonWidget.builder(
                Text.translatable("screen.tokorotenslime.portal_action.delete"),
                button -> controller.onDeleteWaypoint(body.getSelectedOrNull())
        ).width(71).build();
        deleteButton.active = false;
        adder.add(deleteButton);

        // 閉じる
        adder.add(
                ButtonWidget.builder(
                        Text.translatable("screen.tokorotenslime.portal_action.close"),
                        button -> this.close()
                ).width(71).build()
        );
    }

    /**
     * 各widgetの位置を再計算
     */
    @Override
    protected void refreshWidgetPositions() {
        if (this.body != null) {
            this.body.position(this.width, this.layout);
        }
        this.layout.refreshPositions();
    }

    /**
     * ゲームを停止させない
     */
    @Override
    public boolean shouldPause() {
        return false;
    }

    /**
     * ウェイポイントリストを再生成して更新
     */
    public void refreshWaypointList() {
        this.remove(this.body);
        this.body = this.layout.addBody(new WaypointListWidget(this.client, this));
        this.addDrawableChild(this.body);
        updateActionButtons(null);
    }

    /**
     * 選択状態に応じてボタンの有効/無効を更新
     *
     * @param selected 現在選択されているリストエントリ
     */
    public void updateActionButtons(@Nullable WaypointListWidget.Entry selected) {
        controller.updateButtons(
                selected,
                portalPlaceButton,
                quickRegisterButton,
                editButton,
                deleteButton,
                waypointRegisterButton
        );
    }
}
