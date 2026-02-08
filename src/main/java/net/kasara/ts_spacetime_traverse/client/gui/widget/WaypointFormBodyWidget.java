package net.kasara.ts_spacetime_traverse.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kasara.ts_spacetime_traverse.client.data.DimensionClientCache;
import net.kasara.ts_spacetime_traverse.client.gui.screen.WaypointFormScreen;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Waypoint登録・編集画面の「本文部分」を担当するウィジェット
 *
 *  ThreePartsLayoutWidgetのbodyに配置され、
 *  実際の入力欄（名前・座標・向きなど）をまとめて管理する。
 */
@Environment(EnvType.CLIENT)
public class WaypointFormBodyWidget extends ElementListWidget<WaypointFormBodyWidget.WaypointEntry> {

    /** 親となるScreen */
    private final WaypointFormScreen screen;

    // レイアウト用定数
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 150;
    private static final int VERTICAL_SPACING = 4; // 行間
    private static final int XYZ_SPACING = 65; // X,Y,Z横並び用間隔
    private static final int COORDS_WIDTH = 60;

    // 座標欄の入力制限
    private static final Pattern INT_PATTERN = Pattern.compile("-?(0|[1-9]\\d*)?");

    /**
     * コンストラクタ
     *
     * @param client MinecraftClient
     * @param screen 親スクリーン
     * @param data 編集時のWaypointデータ(登録時はnull)
     */
    public WaypointFormBodyWidget(MinecraftClient client, WaypointFormScreen screen, @Nullable WaypointData data) {
        super(client, screen.width, screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), 24);
        this.centerListVertically = false;
        this.screen = screen;

        this.addEntry(new WaypointEntry(client.textRenderer, data));
    }

    /**
     * 画面側から名前入力欄にフォーカス当てるためのヘルパー
     */
    public @Nullable TextFieldWidget getNameField() {
        if (this.children().isEmpty()) return null;
        WaypointEntry entry = this.children().getFirst();
        return entry.getNameField();
    }

    /**
     * 実際の入力UI一式をもつエントリクラス
     */
    @Environment(EnvType.CLIENT)
    public class WaypointEntry extends ElementListWidget.Entry<WaypointEntry> {

        // UI部品
        private final TextWidget nameLabel;
        private final TextFieldWidget nameField;
        private final TextWidget dimensionLabel;
        private final TextFieldWidget dimensionField;

        private final TextWidget xLabel;
        private final TextWidget yLabel;
        private final TextWidget zLabel;
        private final TextFieldWidget xField;
        private final TextFieldWidget yField;
        private final TextFieldWidget zField;

        // 向き(Yaw)を管理
        private WaypointDirection direction = WaypointDirection.SOUTH;
        private final ButtonWidget directionButton;

        // children()で返すためのUI要素一覧
        private final List<Element> elements;

        /**
         * エントリの初期化
         */
        public WaypointEntry(TextRenderer textRenderer, @Nullable WaypointData data) {
            elements = new ArrayList<>();

            // 初期値
            int px = 0, py = 64, pz = 0, yaw = 0;
            String defDimName = "minecraft:overworld";
            String dimName = defDimName;

            // 編集モードの場合
            if (data != null) {
                px = data.blockPos().getX();
                py = data.blockPos().getY();
                pz = data.blockPos().getZ();
                yaw = data.yaw();
                dimName = data.dimension().getValue().toString();
            }
            // 新規登録の場合
            else {
                if (client.player != null) {
                    BlockPos pos = client.player.getBlockPos();
                    px = pos.getX();
                    py = pos.getY();
                    pz = pos.getZ();
                    dimName = client.player.getEntityWorld().getRegistryKey().getValue().toString();
                }
            }

            // 名前
            nameLabel = new TextWidget(Text.translatable("screen.tokorotenslime.waypoint_register.name"), textRenderer);
            nameField = new TextFieldWidget(textRenderer, FIELD_WIDTH, FIELD_HEIGHT, Text.empty());
            nameField.setPlaceholder(Text.translatable("screen.tokorotenslime.waypoint_register.name"));
            if (data != null) nameField.setText(data.name());
            elements.add(nameLabel);
            elements.add(nameField);

            // ディメンション
            dimensionLabel = new TextWidget(Text.translatable("screen.tokorotenslime.waypoint_register.dimension"), textRenderer);
            dimensionField = new TextFieldWidget(textRenderer, FIELD_WIDTH, FIELD_HEIGHT, Text.empty());
            dimensionField.setPlaceholder(Text.literal(defDimName));
            if (!dimName.equals(defDimName)) dimensionField.setText(dimName);
            elements.add(dimensionLabel);
            elements.add(dimensionField);

            // 座標ラベル
            xLabel = new TextWidget(Text.literal("X"), textRenderer);
            yLabel = new TextWidget(Text.literal("Y"), textRenderer);
            zLabel = new TextWidget(Text.literal("Z"), textRenderer);
            elements.add(xLabel);
            elements.add(yLabel);
            elements.add(zLabel);

            // 座標入力
            xField = new TextFieldWidget(textRenderer, COORDS_WIDTH, FIELD_HEIGHT, Text.empty());
            yField = new TextFieldWidget(textRenderer, COORDS_WIDTH, FIELD_HEIGHT, Text.empty());
            zField = new TextFieldWidget(textRenderer, COORDS_WIDTH, FIELD_HEIGHT, Text.empty());
            xField.setPlaceholder(Text.literal("X"));
            yField.setPlaceholder(Text.literal("Y"));
            zField.setPlaceholder(Text.literal("Z"));
            xField.setText(String.valueOf(px));
            yField.setText(String.valueOf(py));
            zField.setText(String.valueOf(pz));
            xField.setTextPredicate(text -> INT_PATTERN.matcher(text).matches());
            yField.setTextPredicate(text -> INT_PATTERN.matcher(text).matches());
            zField.setTextPredicate(text -> INT_PATTERN.matcher(text).matches());

            elements.add(xField);
            elements.add(yField);
            elements.add(zField);

            // 向きボタン
            if (data != null) direction = directionFromYaw(yaw);
            directionButton = ButtonWidget.builder(
                    Text.translatable("screen.tokorotenslime.waypoint_register.direction", direction.text()),
                    button -> {
                        direction = direction.next();
                        button.setMessage(Text.translatable("screen.tokorotenslime.waypoint_register.direction", direction.text()));
                    }
            ).width(BUTTON_WIDTH).build();
            elements.add(directionButton);
        }

        /**
         * 各フレームごとの描画処理
         */
        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int xCenter = screen.width / 2;
            int y = this.getContentY() + VERTICAL_SPACING;

            // 名前
            nameLabel.setX(xCenter - nameLabel.getWidth() / 2);
            nameLabel.setY(y);
            nameLabel.render(context, mouseX, mouseY, deltaTicks);
            y += FIELD_HEIGHT - VERTICAL_SPACING * 2;

            // 名前入力
            nameField.setPosition(xCenter - nameField.getWidth() / 2, y);
            nameField.render(context, mouseX, mouseY, deltaTicks);
            y += FIELD_HEIGHT + VERTICAL_SPACING * 3;

            // ディメンション
            dimensionLabel.setX(xCenter - dimensionLabel.getWidth() / 2);
            dimensionLabel.setY(y);
            dimensionLabel.render(context, mouseX, mouseY, deltaTicks);
            y += FIELD_HEIGHT - VERTICAL_SPACING * 2;

            // ディメンション入力
            dimensionField.setPosition(xCenter - dimensionField.getWidth() / 2, y);
            dimensionField.render(context, mouseX, mouseY, deltaTicks);
            y += FIELD_HEIGHT + VERTICAL_SPACING * 3;

            // 座標ラベル
            xLabel.setX(xCenter - xLabel.getWidth() / 2 - XYZ_SPACING);
            xLabel.setY(y);
            xLabel.render(context, mouseX, mouseY, deltaTicks);

            yLabel.setX(xCenter - yLabel.getWidth() / 2);
            yLabel.setY(y);
            yLabel.render(context, mouseX, mouseY, deltaTicks);

            zLabel.setX(xCenter - zLabel.getWidth() / 2 + XYZ_SPACING);
            zLabel.setY(y);
            zLabel.render(context, mouseX, mouseY, deltaTicks);
            y += FIELD_HEIGHT - VERTICAL_SPACING * 2;

            // 座標入力
            xField.setPosition(xCenter - xField.getWidth() / 2 - XYZ_SPACING, y);
            yField.setPosition(xCenter - yField.getWidth() / 2, y);
            zField.setPosition(xCenter - zField.getWidth() / 2 + XYZ_SPACING, y);
            xField.render(context, mouseX, mouseY, deltaTicks);
            yField.render(context, mouseX, mouseY, deltaTicks);
            zField.render(context, mouseX, mouseY, deltaTicks);
            y += FIELD_HEIGHT + VERTICAL_SPACING * 3;

            // 向きボタン
            directionButton.setPosition(xCenter - directionButton.getWidth() / 2, y);
            directionButton.render(context, mouseX, mouseY, deltaTicks);
        }

        /**
         * 入力・クリック対象の UI 一覧
         */
        @Override
        public List<? extends Element> children() {
            return elements;
        }

        /**
         * キーボード操作でフォーカス可能な要素
         */
        @Override
        public List<? extends Selectable> selectableChildren() {
            // nameField, xField, yField, zField, directionButton は Selectable
            return List.of(nameField, xField, yField, zField, directionButton);
        }

        /**
         * スクロール計算用高さ
         */
        @Override
        public int getHeight() {
            // 適当な高さでスクロール計算に影響
            return (int) (directionButton.getHeight() * 8.7);
        }

        /**
         * 向き管理用enum
         */
        public enum WaypointDirection {
            NORTH("screen.tokorotenslime.waypoint_register.north", 180),
            EAST("screen.tokorotenslime.waypoint_register.east", -90),
            SOUTH("screen.tokorotenslime.waypoint_register.south", 0),
            WEST("screen.tokorotenslime.waypoint_register.west", 90);

            private final String key;
            private final int yaw;

            WaypointDirection(String key, int yaw) {
                this.key = key;
                this.yaw = yaw;
            }

            public Text text() {
                return Text.translatable(key);
            }

            public int yaw() {
                return yaw;
            }

            public WaypointDirection next() {
                WaypointDirection[] values = values();
                return values[(this.ordinal() + 1) % values.length];
            }
        }

        private WaypointDirection directionFromYaw(int yaw) {
            for (WaypointDirection dir : WaypointDirection.values()) {
                if (dir.yaw() == yaw) return dir;
            }
            return WaypointDirection.SOUTH;
        }

        /**
         * 入力内容が有効か判定する
         */
        public boolean isValid() {
            if (nameField.getText().isBlank()) return false;

            if (client.world == null) return false;

            if (!isValidDimensionId(dimensionField.getText())) return false;

            WorldBorder border = client.world.getWorldBorder();

            int minX = (int) Math.floor(border.getBoundWest());
            int maxX = (int) Math.ceil(border.getBoundEast());
            int minZ = (int) Math.floor(border.getBoundNorth());
            int maxZ = (int) Math.ceil(border.getBoundSouth());

            int minY = client.world.getBottomY();
            int maxY = client.world.getDimension().height() + minY - 1;

            return isValidCoord(xField, minX, maxX)
                    && isValidCoord(yField, minY, maxY)
                    && isValidCoord(zField, minZ, maxZ);
        }

        /**
         * 座標1つ分の妥当性チェック
         */
        private boolean isValidCoord(TextFieldWidget field, int worldMin, int worldMax) {
            String text = field.getText();
            if (text.isEmpty() || text.equals("-")) return false;

            if (text.matches("-0+")) return false;

            try {
                int value = Integer.parseInt(text);
                return value >= worldMin && value <= worldMax;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private boolean isValidDimensionId(String text) {
            if (text.isBlank()) return true;

            Identifier id;
            try {
                id = Identifier.of(text);
            } catch (Exception e) {
                return false;
            }

            return DimensionClientCache.contains(id);
        }

        // === WaypointData 作成用 getter ===

        public String getWaypointName() {
            return nameField.getText();
        }

        public int getWaypointX() {
            return Integer.parseInt(xField.getText());
        }

        public int getWaypointY() {
            return Integer.parseInt(yField.getText());
        }

        public int getWaypointZ() {
            return Integer.parseInt(zField.getText());
        }

        public int getWaypointYaw() {
            return direction.yaw();
        }

        public RegistryKey<World> getWaypointDimension() {
            String text = dimensionField.getText();

            if (text.isBlank()) return World.OVERWORLD;

            return RegistryKey.of(RegistryKeys.WORLD, Identifier.of(text));
        }

        public TextFieldWidget getNameField() {
            return this.nameField;
        }
    }
}