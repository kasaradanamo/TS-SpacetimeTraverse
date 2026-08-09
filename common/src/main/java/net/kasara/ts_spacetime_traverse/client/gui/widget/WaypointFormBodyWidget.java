package net.kasara.ts_spacetime_traverse.client.gui.widget;

import net.kasara.ts_spacetime_traverse.client.data.DimensionClientCache;
import net.kasara.ts_spacetime_traverse.client.gui.GuiBackgrounds;
import net.kasara.ts_spacetime_traverse.client.gui.screen.WaypointFormScreen;
import net.kasara.ts_spacetime_traverse.util.DimensionBounds;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Waypoint登録・編集画面の「本文部分」を担当するウィジェット
 *
 *  実際の入力欄（名前・座標・向きなど）をまとめて管理する。
 */
public class WaypointFormBodyWidget extends ContainerObjectSelectionList<WaypointFormBodyWidget.WaypointEntry> {

    // 親となるScreen
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
     * @param minecraft MinecraftClient
     * @param screen    親スクリーン
     * @param data      編集時のWaypointデータ(登録時はnull)
     */
    public WaypointFormBodyWidget(Minecraft minecraft, WaypointFormScreen screen, @Nullable WaypointData data) {
        // 1.20.1は26.2と違いエントリごとの可変高さが無いため、フォーム全体の高さをitemHeightにする
        super(minecraft, screen.width, screen.height,
                WaypointFormScreen.HEADER_HEIGHT,
                screen.height - WaypointFormScreen.FOOTER_HEIGHT,
                174);
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        this.screen = screen;

        this.addEntry(new WaypointEntry(minecraft.font, data));
    }

    /**
     * 26.2相当のリスト背景と区切り線を描く
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiBackgrounds.drawListBackground(graphics, this.x0, this.x1, this.y0, this.y1);

        super.render(graphics, mouseX, mouseY, partialTick);

        GuiBackgrounds.drawListSeparators(graphics, this.x0, this.x1, this.y0, this.y1);
    }

    /**
     * 画面側から名前入力欄にフォーカス当てるためのヘルパー
     */
    public EditBox getNameField() {
        if (this.children().isEmpty()) return null;
        WaypointEntry entry = this.children().get(0);
        return entry.getNameField();
    }

    /**
     * 実際の入力UI一式をもつエントリクラス
     */
    public class WaypointEntry extends ContainerObjectSelectionList.Entry<WaypointEntry> {

        // UI部品
        private final StringWidget nameLabel;
        private final EditBox nameField;
        private final StringWidget dimensionLabel;
        private final EditBox dimensionField;

        private final StringWidget xLabel;
        private final StringWidget yLabel;
        private final StringWidget zLabel;
        private final EditBox xField;
        private final EditBox yField;
        private final EditBox zField;

        // 向き(Yaw)を管理
        private WaypointDirection direction = WaypointDirection.SOUTH;
        private final Button directionButton;

        // children()で返すためのUI要素一覧
        private final List<AbstractWidget> elements;

        /**
         * エントリの初期化
         */
        public WaypointEntry(Font font, @Nullable WaypointData data) {
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
                dimName = data.dimension().location().toString();
            }
            // 新規登録の場合
            else {
                if (minecraft.player != null) {
                    BlockPos pos = minecraft.player.blockPosition();
                    px = pos.getX();
                    py = pos.getY();
                    pz = pos.getZ();
                    dimName = minecraft.player.level().dimension().location().toString();
                }
            }

            // 名前
            nameLabel = new StringWidget(Component.translatable("screen.tokorotenslime.waypoint_register.name"), font);
            nameField = new EditBox(font, 0, 0, FIELD_WIDTH, FIELD_HEIGHT, Component.empty());
            nameField.setHint(Component.translatable("screen.tokorotenslime.waypoint_register.name"));
            if (data != null) nameField.setValue(data.name());
            elements.add(nameLabel);
            elements.add(nameField);

            // ディメンション
            dimensionLabel = new StringWidget(Component.translatable("screen.tokorotenslime.waypoint_register.dimension"), font);
            dimensionField = new EditBox(font, 0, 0, FIELD_WIDTH, FIELD_HEIGHT, Component.empty());
            dimensionField.setHint(Component.literal(defDimName));
            if (!dimName.equals(defDimName)) dimensionField.setValue(dimName);
            elements.add(dimensionLabel);
            elements.add(dimensionField);

            // 座標ラベル
            xLabel = new StringWidget(Component.literal("X"), font);
            yLabel = new StringWidget(Component.literal("Y"), font);
            zLabel = new StringWidget(Component.literal("Z"), font);
            elements.add(xLabel);
            elements.add(yLabel);
            elements.add(zLabel);

            // 座標入力
            xField = new EditBox(font, 0, 0, COORDS_WIDTH, FIELD_HEIGHT, Component.empty());
            yField = new EditBox(font, 0, 0, COORDS_WIDTH, FIELD_HEIGHT, Component.empty());
            zField = new EditBox(font, 0, 0, COORDS_WIDTH, FIELD_HEIGHT, Component.empty());
            xField.setHint(Component.literal("X"));
            yField.setHint(Component.literal("Y"));
            zField.setHint(Component.literal("Z"));
            xField.setValue(String.valueOf(px));
            yField.setValue(String.valueOf(py));
            zField.setValue(String.valueOf(pz));

            elements.add(xField);
            elements.add(yField);
            elements.add(zField);

            // 向きボタン
            if (data != null) direction = directionFromYaw(yaw);
            directionButton = Button.builder(
                    Component.translatable("screen.tokorotenslime.waypoint_register.direction", direction.text()),
                    button -> {
                        direction = direction.next();
                        button.setMessage(Component.translatable("screen.tokorotenslime.waypoint_register.direction", direction.text()));
                    }
            ).width(BUTTON_WIDTH).build();
            elements.add(directionButton);
        }

        /**
         * 各フレームごとの描画処理
         */
        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int xCenter = screen.width / 2;
            int y = top + VERTICAL_SPACING;

            // 名前
            nameLabel.setX(xCenter - nameLabel.getWidth() / 2);
            nameLabel.setY(y);
            nameLabel.render(graphics, mouseX, mouseY, partialTick);
            y += FIELD_HEIGHT - VERTICAL_SPACING * 2;

            // 名前入力
            nameField.setPosition(xCenter - nameField.getWidth() / 2, y);
            nameField.render(graphics, mouseX, mouseY, partialTick);
            y += FIELD_HEIGHT + VERTICAL_SPACING * 3;

            // ディメンション
            dimensionLabel.setX(xCenter - dimensionLabel.getWidth() / 2);
            dimensionLabel.setY(y);
            dimensionLabel.render(graphics, mouseX, mouseY, partialTick);
            y += FIELD_HEIGHT - VERTICAL_SPACING * 2;

            // ディメンション入力
            dimensionField.setPosition(xCenter - dimensionField.getWidth() / 2, y);
            dimensionField.render(graphics, mouseX, mouseY, partialTick);
            y += FIELD_HEIGHT + VERTICAL_SPACING * 3;

            // 座標ラベル
            xLabel.setX(xCenter - xLabel.getWidth() / 2 - XYZ_SPACING);
            xLabel.setY(y);
            xLabel.render(graphics, mouseX, mouseY, partialTick);

            yLabel.setX(xCenter - yLabel.getWidth() / 2);
            yLabel.setY(y);
            yLabel.render(graphics, mouseX, mouseY, partialTick);

            zLabel.setX(xCenter - zLabel.getWidth() / 2 + XYZ_SPACING);
            zLabel.setY(y);
            zLabel.render(graphics, mouseX, mouseY, partialTick);
            y += FIELD_HEIGHT - VERTICAL_SPACING * 2;

            // 座標入力
            xField.setPosition(xCenter - xField.getWidth() / 2 - XYZ_SPACING, y);
            yField.setPosition(xCenter - yField.getWidth() / 2, y);
            zField.setPosition(xCenter - zField.getWidth() / 2 + XYZ_SPACING, y);
            xField.render(graphics, mouseX, mouseY, partialTick);
            yField.render(graphics, mouseX, mouseY, partialTick);
            zField.render(graphics, mouseX, mouseY, partialTick);
            y += FIELD_HEIGHT + VERTICAL_SPACING * 3;

            // 向きボタン
            directionButton.setPosition(xCenter - directionButton.getWidth() / 2, y);
            directionButton.render(graphics, mouseX, mouseY, partialTick);
        }

        /**
         * 入力・クリック対象の UI 一覧
         */
        @Override
        public List<? extends NarratableEntry> narratables() {
            return elements;
        }

        /**
         * キーボード操作でフォーカス可能な要素
         */
        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(nameField, dimensionField, xField, yField, zField, directionButton);
        }

        /**
         * EditBoxのカーソル点滅用tick(1.20.1のみ必要)
         */
        public void tickFields() {
            nameField.tick();
            dimensionField.tick();
            xField.tick();
            yField.tick();
            zField.tick();
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

            public Component text() {
                return Component.translatable(key);
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
            if (nameField.getValue().isBlank()) return false;

            String dimText = dimensionField.getValue();
            if (!isValidDimensionId(dimText)) return false;

            ResourceLocation id = dimText.isBlank() ? Level.OVERWORLD.location() : ResourceLocation.tryParse(dimText);
            if (id == null) return false;

            DimensionBounds info = DimensionClientCache.get(id);
            if (info == null) return false;

            int minX = (int) Math.floor(info.minX());
            int maxX = (int) Math.ceil(info.maxX());
            int minZ = (int) Math.floor(info.minZ());
            int maxZ = (int) Math.ceil(info.maxZ());

            int minY = info.minY();
            int maxY = info.maxY();

            return isValidCoord(xField, minX, maxX)
                    && isValidCoord(yField, minY, maxY)
                    && isValidCoord(zField, minZ, maxZ);
        }

        /**
         * 座標1つ分の妥当性チェック
         */
        private boolean isValidCoord(EditBox field, int worldMin, int worldMax) {
            String text = field.getValue();

            // 空や単独マイナスは不可
            if (text.isEmpty() || text.equals("-")) return false;

            // 数値形式チェック
            if (!INT_PATTERN.matcher(text).matches()) return false;

            // -000みたいなの禁止
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

            ResourceLocation id = ResourceLocation.tryParse(text);
            if (id == null) return false;

            return DimensionClientCache.contains(id);
        }

        // === WaypointData 作成用 getter ===

        public String getWaypointName() {
            return nameField.getValue();
        }

        public int getWaypointX() {
            return Integer.parseInt(xField.getValue());
        }

        public int getWaypointY() {
            return Integer.parseInt(yField.getValue());
        }

        public int getWaypointZ() {
            return Integer.parseInt(zField.getValue());
        }

        public int getWaypointYaw() {
            return direction.yaw();
        }

        public ResourceKey<Level> getWaypointDimension() {
            String text = dimensionField.getValue();

            if (text.isBlank()) return Level.OVERWORLD;

            return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(text));
        }

        public EditBox getNameField() {
            return this.nameField;
        }
    }
}
