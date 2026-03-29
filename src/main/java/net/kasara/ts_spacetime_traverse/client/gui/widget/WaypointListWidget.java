package net.kasara.ts_spacetime_traverse.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.client.data.WaypointClientCache;
import net.kasara.ts_spacetime_traverse.client.gui.screen.PortalActionScreen;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * PortalActionScreen内のウェイポイントリスト
 */
@Environment(EnvType.CLIENT)
public class WaypointListWidget extends ObjectSelectionList<WaypointListWidget.Entry> {

    // このリストを持つScreen
    public final PortalActionScreen parentScreen;

    public WaypointListWidget(Minecraft minecraft, PortalActionScreen screen) {
        super(minecraft, screen.width, screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), 24);
        this.centerListVertically = false;
        this.parentScreen = screen;

        // ClientWaypointManagerから全ウェイポイントを取得してEntryを追加
        WaypointClientCache.getAll().forEach(data -> this.addEntry(new Entry(this, data, minecraft)));
    }

    /**
     * リストの各エントリ
     */
    @Environment(EnvType.CLIENT)
    public static class Entry extends ObjectSelectionList.Entry<WaypointListWidget.Entry> {

        private static final Identifier TRUE_QUICK_ICON =
                Identifier.fromNamespaceAndPath(TokorotenSlimeAPI.getModId(), "textures/item/spacetime_eye.png");
        private static final Identifier FALSE_QUICK_ICON =
                Identifier.fromNamespaceAndPath(TokorotenSlimeAPI.getModId(), "textures/gui/false_quick_icon.png");

        private final WaypointListWidget parent;
        public final WaypointData data;
        private final StringWidget nameWidget;
        private final StringWidget dimensionWidget;
        private final StringWidget positionWidget;

        public Entry(WaypointListWidget parent, WaypointData data, Minecraft minecraft) {
            this.parent = parent;
            this.data = data;

            // 名前、ディメンション、座標のテキストウィジェットを作成
            this.nameWidget = new StringWidget(Component.literal(data.name()).withStyle(ChatFormatting.WHITE), minecraft.font);
            this.dimensionWidget = new StringWidget(Component.literal(data.dimension().identifier().toString()).withStyle(ChatFormatting.GRAY), minecraft.font);
            this.positionWidget = new StringWidget(
                    Component.literal("XYZ: " + data.blockPos().getX() +
                            " / " + data.blockPos().getY() +
                            " / " + data.blockPos().getZ()).withStyle(ChatFormatting.GRAY),
                    minecraft.font
            );
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int x = this.getContentX();
            int y = this.getContentY();
            int w = this.getWidth();
            int h = this.getHeight();

            int nx = x - 2;
            int ny = y - 2;

            // ホバー時の背景描画
            if (hovered) {
                graphics.fill(nx, ny, nx + w , ny + h, 0x40FFFFFF);
                drawBorder(graphics, nx, ny, w, h, 0xFFFFFFFF);
            }

            UUID quick = WaypointClientCache.getQuick();

            // クイックアイコン描画
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    data.uuid().equals(quick) ? TRUE_QUICK_ICON : FALSE_QUICK_ICON,
                    x + 4, y + 2, 0, 0, 16, 16, 16, 16
            );

            int textX = x + 29;

            // 名前描画
            this.nameWidget.setPosition(textX, y + 1);
            this.nameWidget.setMessage(Component.literal(data.name()).withStyle(
                    data.uuid().equals(quick) ? ChatFormatting.YELLOW : ChatFormatting.WHITE
            ));
            this.nameWidget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);

            // 座標描画
            this.positionWidget.setPosition(textX, y + 11);
            this.positionWidget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);

            int maxWidth = getWidth() - 30; // ざっくり
            this.nameWidget.setMaxWidth(maxWidth);
            this.dimensionWidget.setMaxWidth(maxWidth);
            this.positionWidget.setMaxWidth(maxWidth);
        }

        @Override
        public Component getNarration() {
            return Component.literal("list");
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() == 0) {
                // 選択とボタン状態更新
                parent.setSelected(this);
                parent.parentScreen.updateActionButtons(this);
                return true;
            }
            return false;
        }

        /**
         * Entry の枠線描画
         */
        private static void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
            graphics.fill(x, y, x + w, y + 1, color);                // 上
            graphics.fill(x, y + h, x + w, y + h - 1, color);    // 下
            graphics.fill(x, y, x + 1, y + h, color);                // 左
            graphics.fill(x + w - 1, y, x + w, y + h, color);    // 右
        }
    }
}