package net.kasara.ts_spacetime_traverse.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kasara.ts_spacetime_traverse.client.PortalActionController;
import net.kasara.ts_spacetime_traverse.client.data.WaypointClientCache;
import net.kasara.ts_spacetime_traverse.client.gui.widget.WaypointListWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * ポータル操作用のメイン画面
 */
@Environment(EnvType.CLIENT)
public class PortalActionScreen extends Screen {

    // 3分割レイアウト(ヘッダー/ボディ/フッター)
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 4, 60);

    // 中央のウェイポイントリスト
    private WaypointListWidget body;

    // ボタン群
    protected Button waypointRegisterButton;
    protected Button portalPlaceButton;
    protected Button quickRegisterButton;
    protected Button editButton;
    protected Button deleteButton;

    // ボタン押下などの処理を担当するコントローラ
    private final PortalActionController controller;

    public PortalActionScreen() {
        super(Component.translatable("screen.tokorotenslime.portal_action.title"));
        this.controller = new PortalActionController(this);
    }

    @Override
    protected void init() {
        this.initHeader();
        this.initBody();
        this.initFooter();

        // layout内widgetをまとめて追加
        this.layout.visitWidgets(this::addRenderableWidget);

        // 位置計算
        this.repositionElements();
    }

    private void initHeader() {
        this.layout.addTitleHeader(this.title, this.font);
    }

    private void initBody() {
        this.body = this.layout.addToContents(new WaypointListWidget(this.minecraft, this));
    }

    private void initFooter() {
        GridLayout grid = this.layout.addToFooter(new GridLayout().columnSpacing(8).rowSpacing(4));
        grid.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper rowHelper = grid.createRowHelper(4);

        // 1段目（メイン操作）
        // ポータル設置
        portalPlaceButton = Button.builder(
                Component.translatable("screen.tokorotenslime.portal_action.place"),
                button -> controller.onPlacePortal(body.getSelected())
        ).build();
        portalPlaceButton.active = false;
        rowHelper.addChild(portalPlaceButton, 2);

        // ウェイポイント登録
        waypointRegisterButton = Button.builder(
                Component.translatable("screen.tokorotenslime.waypoint_register.title"),
                button -> controller.onOpenWaypointFormRegister()
        ).build();
        waypointRegisterButton.active = WaypointClientCache.getAll().size() < 10;
        rowHelper.addChild(waypointRegisterButton, 2);


        // 2段目（補助）
        // クイック登録
        quickRegisterButton = Button.builder(
                Component.translatable("screen.tokorotenslime.portal_action.quick_register"),
                button -> controller.onQuickRegister(body.getSelected())
        ).width(71).build();
        quickRegisterButton.active = false;
        rowHelper.addChild(quickRegisterButton);

        // 編集
        editButton = Button.builder(
                Component.translatable("screen.tokorotenslime.portal_action.edit"),
                button -> controller.onEditWaypoint(body.getSelected())
        ).width(71).build();
        editButton.active = false;
        rowHelper.addChild(editButton);

        // 削除
        deleteButton = Button.builder(
                Component.translatable("screen.tokorotenslime.portal_action.delete"),
                button -> controller.onDeleteWaypoint(body.getSelected())
        ).width(71).build();
        deleteButton.active = false;
        rowHelper.addChild(deleteButton);

        // 閉じる
        rowHelper.addChild(
                Button.builder(
                        Component.translatable("screen.tokorotenslime.portal_action.close"),
                        button -> this.onClose()
                ).width(71).build()
        );
    }

    /**
     * 各widgetの位置を再計算
     */
    @Override
    protected void repositionElements() {
        if (this.body != null) {
            this.body.updateSize(this.width, this.layout);
        }
        this.layout.arrangeElements();
    }

    /**
     * ゲームを停止させない
     */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * ウェイポイントリストを再生成して更新
     */
    public void refreshWaypointList() {
        this.removeWidget(this.body);
        this.body = this.layout.addToContents(new WaypointListWidget(this.minecraft, this));
        this.addRenderableWidget(this.body);
        updateActionButtons(null);
    }

    /**
     * 選択状態に応じてボタンの有効/無効を更新
     *
     * @param selected 現在選択されているリストエントリ
     */
    public void updateActionButtons(WaypointListWidget.@Nullable Entry selected) {
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