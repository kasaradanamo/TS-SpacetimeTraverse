package net.kasara.ts_spacetime_traverse.client.gui.screen;

import net.kasara.ts_spacetime_traverse.client.PortalActionController;
import net.kasara.ts_spacetime_traverse.client.data.WaypointClientCache;
import net.kasara.ts_spacetime_traverse.client.gui.GuiBackgrounds;
import net.kasara.ts_spacetime_traverse.client.gui.widget.WaypointListWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

/**
 * ポータル操作用のメイン画面
 */
public class PortalActionScreen extends Screen {

    // 3分割レイアウト相当の領域定義(26.2のHeaderAndFooterLayout(8+9+8+4, 60)と同じ値)
    public static final int HEADER_HEIGHT = 8 + 9 + 8 + 4;
    public static final int FOOTER_HEIGHT = 60;

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
        this.initBody();
        this.initFooter();
    }

    private void initBody() {
        this.body = new WaypointListWidget(this.minecraft, this);
        this.addRenderableWidget(this.body);
    }

    private void initFooter() {
        GridLayout grid = new GridLayout().columnSpacing(8).rowSpacing(4);
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

        // フッター領域の中央に配置してScreenへ登録
        grid.arrangeElements();
        FrameLayout.centerInRectangle(grid, 0, this.height - FOOTER_HEIGHT, this.width, FOOTER_HEIGHT);
        grid.visitWidgets(this::addRenderableWidget);
    }

    /**
     * ワールド内では26.2と同じく一様な半透明で塗る(バニラのグラデーションは使わない)
     */
    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        if (this.minecraft != null && this.minecraft.level != null) {
            GuiBackgrounds.drawMenuBackground(guiGraphics, this.width, this.height);
        } else {
            super.renderBackground(guiGraphics);
        }
    }

    /**
     * ヘッダー(タイトル)は自前で描画する
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, (HEADER_HEIGHT - 9) / 2 + 4, 0xFFFFFF);
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
        this.body = new WaypointListWidget(this.minecraft, this);
        this.addRenderableWidget(this.body);
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
