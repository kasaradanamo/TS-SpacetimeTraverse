package net.kasara.ts_spacetime_traverse.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kasara.ts_spacetime_traverse.client.WaypointClientManager;
import net.kasara.ts_spacetime_traverse.client.gui.widget.WaypointFormBodyWidget;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.kasara.ts_spacetime_traverse.util.WaypointDataFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
@Environment(EnvType.CLIENT)
public class WaypointFormScreen extends Screen {

    // 親画面(PortalActionScreen)
    private final Screen parent;

    // 登録or編集モード
    private final Mode mode;

    // 編集対象のデータ(登録時はnull)
    @Nullable private final WaypointData data;

    /** 画面全体のレイアウト */
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, 8 + 9 + 8 + 4);

    // 入力フォームウィジェット
    private WaypointFormBodyWidget body;

    // 登録/保存ボタン
    private ButtonWidget confirmButton;

    /** 画面動作モード */
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
        super(Text.translatable(
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
        this.layout.forEachChild(widget -> this.addDrawableChild((ClickableWidget) widget));

        // 画面サイズに応じて位置計算
        this.refreshWidgetPositions();

        // 最初に名前入力欄へフォーカスを当てる
        this.setInitialFocusToName();
    }

    /**
     * 上部：タイトル
     */
    private void initHeader() {
        this.layout.addHeader(this.title, this.textRenderer);
    }

    /**
     * 中央：入力フォーム
     * data が null → 登録用
     * data がある  → 編集用
     */
    private void initBody() {
        this.body = this.layout.addBody(new WaypointFormBodyWidget(this.client, this, data));
    }

    /**
     * 下部：ボタン類
     */
    private void initFooter() {
        GridWidget grid = this.layout.addFooter(new GridWidget().setColumnSpacing(8));

        grid.getMainPositioner().alignHorizontalCenter();
        GridWidget.Adder adder = grid.createAdder(2);

        // 登録/保存ボタン
        confirmButton = ButtonWidget.builder(
                Text.translatable(
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
                    WaypointData data = WaypointDataFactory.fromInputs(
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
                    Objects.requireNonNull(this.client).setScreen(this.parent);
                }
        ).width(120).build();
        confirmButton.active = false;
        adder.add(confirmButton);

        // 戻るボタン
        adder.add(
                ButtonWidget.builder(
                        ScreenTexts.BACK,
                        button -> this.close()
                ).width(120).build()
        );
    }

    /**
     * レイアウト位置再計算
     */
    @Override
    protected void refreshWidgetPositions() {
        if (this.body != null) {
            this.body.position(this.width, this.layout);
        }
        this.layout.refreshPositions();
    }

    /**
     * 画面を閉じるときは親画面へ戻る
     */
    @Override
    public void close() {
        Objects.requireNonNull(this.client).setScreen(this.parent);
    }

    /**
     * ゲームを停止しない
     */
    @Override
    public boolean shouldPause() {
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

        TextFieldWidget nameField = this.body.getNameField();
        if (nameField == null) return;

        this.setInitialFocus(nameField);
        nameField.setFocused(true);
    }

}