package net.kasara.ts_spacetime_traverse.client.gui.screen;

import net.kasara.ts_spacetime_traverse.client.WaypointClientManager;
import net.kasara.ts_spacetime_traverse.client.gui.widget.WaypointFormBodyWidget;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.kasara.ts_spacetime_traverse.util.WaypointDataUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * ウェイポイントの「登録」と「編集」を共通で扱う画面
 *
 * - Mode.REGISTER : 新規登録
 * - Mode.EDIT     : 既存ウェイポイントの編集
 *
 * UI構成は同じで、
 * ・タイトル
 * ・初期値
 * ・保存時のUUID
 * だけが切り替わる
 */
public class WaypointFormScreen extends Screen {

    // 親画面(PortalActionScreen)
    private final Screen parent;

    // 登録or編集モード
    private final Mode mode;

    // 編集対象のデータ(登録時はnull)
    private final WaypointData data;

    // 画面全体のレイアウト
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 8 + 9 + 8 + 4);

    // 入力フォームウィジェット
    private WaypointFormBodyWidget body;

    // 登録/保存ボタン
    private Button confirmButton;

    // 画面動作モード
    public enum Mode {
        REGISTER,
        EDIT
    }

    /**
     * 実体コンストラクタ
     *
     * @param parent 戻り先画面
     * @param mode 登録or編集
     * @param data 編集対象(登録時はnull)
     */
    public WaypointFormScreen(Screen parent, Mode mode, @Nullable WaypointData data) {
        // タイトルはモードで切替
        super(Component.translatable(
                mode == Mode.REGISTER
                        ? "screen.tokorotenslime.waypoint_register.title"
                        : "screen.tokorotenslime.waypoint_edit.title"
        ));
        this.parent = parent;
        this.mode = mode;
        this.data = data;
    }

    /**
     * 登録画面を開く用
     */
    public static WaypointFormScreen register(Screen parent) {
        return new WaypointFormScreen(parent, Mode.REGISTER, null);
    }

    /**
     * 編集画面を開く用
     */
    public static WaypointFormScreen edit(Screen parent, WaypointData data) {
        return new WaypointFormScreen(parent, Mode.EDIT, data);
    }

    /**
     * 画面の初期化
     */
    @Override
    protected void init() {
        this.initHeader();
        this.initBody();
        this.initFooter();

        // layout に登録された widget をまとめて Screen に追加
        this.layout.visitWidgets(this::addRenderableWidget);

        // 画面サイズに応じて位置計算
        this.repositionElements();

        // 最初に名前入力欄へフォーカスを当てる
        this.setInitialFocusToName();
    }

    /**
     * 上部：タイトル
     */
    private void initHeader() {
        this.layout.addTitleHeader(this.title, this.font);
    }

    /**
     * 中央：入力フォーム
     * data が null → 登録用
     * data がある  → 編集用
     */
    private void initBody() {
        this.body = this.layout.addToContents(new WaypointFormBodyWidget(this.minecraft, this, data));
    }

    /**
     * 下部：ボタン類
     */
    private void initFooter() {
        GridLayout grid = this.layout.addToFooter(new GridLayout().columnSpacing(8));

        grid.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper rowHelper = grid.createRowHelper(2);

        // 登録/保存ボタン
        confirmButton = Button.builder(
                Component.translatable(
                        mode == Mode.REGISTER
                                ? "screen.tokorotenslime.waypoint_register.title"
                                : "screen.tokorotenslime.waypoint_edit.save"
                ),
                button -> {
                    // 念のため null チェック
                    if (body == null || body.children().isEmpty()) return;

                    // 入力不正確認
                    WaypointFormBodyWidget.WaypointEntry entry = body.children().getFirst();
                    if (!entry.isValid()) return;

                    // 入力内容から WaypointData を生成
                    WaypointData data = WaypointDataUtil.fromInputs(
                            mode == Mode.EDIT ? this.data.uuid() : null,
                            entry.getWaypointName(),
                            entry.getWaypointDimension(),
                            entry.getWaypointX(),
                            entry.getWaypointY(),
                            entry.getWaypointZ(),
                            entry.getWaypointYaw()
                    );

                    // クライアントキャッシュ更新 + サーバー送信
                    WaypointClientManager.applyWaypointChange(data, false);

                    // 親画面の一覧を更新
                    if (this.parent instanceof PortalActionScreen portal) {
                        portal.refreshWaypointList();
                        portal.updateActionButtons(null);
                    }

                    // 親画面へ戻る
                    this.minecraft.setScreen(this.parent);
                }
        ).width(120).build();
        confirmButton.active = false;
        rowHelper.addChild(confirmButton);

        // 戻るボタン
        rowHelper.addChild(
                Button.builder(
                        CommonComponents.GUI_BACK,
                        button -> this.onClose()
                ).width(120).build()
        );
    }

    /**
     * レイアウト位置再計算
     */
    @Override
    protected void repositionElements() {
        if (this.body != null) {
            this.body.updateSize(this.width, this.layout);
        }
        this.layout.arrangeElements();
    }

    /**
     * 画面を閉じるときは親画面へ戻る
     */
    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    /**
     * ゲームを停止しない
     */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * 毎tick確認
     * 入力が有効かどうかでボタンを有効/無効切り替える
     */
    @Override
    public void tick() {
        super.tick();

        if (body != null && confirmButton != null) {
            boolean valid = false;

            if (!body.children().isEmpty()) {
                WaypointFormBodyWidget.WaypointEntry entry = body.children().getFirst();
                valid = entry.isValid();
            }

            confirmButton.active = valid;
        }
    }

    /**
     * 画面表示時に名前入力欄へフォーカスを当てる
     */
    private void setInitialFocusToName() {
        if (this.body == null) return;

        EditBox nameField = this.body.getNameField();
        if (nameField == null) return;

        this.setInitialFocus(nameField);
        nameField.setFocused(true);
    }
}