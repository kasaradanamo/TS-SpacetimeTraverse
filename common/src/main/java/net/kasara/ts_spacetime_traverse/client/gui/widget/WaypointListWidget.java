package net.kasara.ts_spacetime_traverse.client.gui.widget;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.client.data.WaypointClientCache;
import net.kasara.ts_spacetime_traverse.client.gui.screen.PortalActionScreen;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.kasara.ts_spacetime_traverse.client.gui.GuiBackgrounds;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * PortalActionScreen内のウェイポイントリスト
 */
public class WaypointListWidget extends ObjectSelectionList<WaypointListWidget.Entry> {

    // このリストを持つScreen
    public final PortalActionScreen parentScreen;

    public WaypointListWidget(Minecraft minecraft, PortalActionScreen screen) {
        super(minecraft, screen.width, screen.height,
                PortalActionScreen.HEADER_HEIGHT,
                screen.height - PortalActionScreen.FOOTER_HEIGHT,
                24);

        this.parentScreen = screen;

        // バニラ既定の土テクスチャ背景・上下の帯を描かない(26.2相当のものをrenderで描く)
        setRenderBackground(false);
        setRenderTopAndBottom(false);

        // ClientWaypointManagerから全ウェイポイントを取得してEntryを追加
        WaypointClientCache.getAll().forEach(data -> this.addEntry(new Entry(this, data, minecraft)));
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
     * 左右2px広げる
     */
    @Override
    protected void renderSelection(net.minecraft.client.gui.GuiGraphics graphics, int top, int width, int height, int outlineColor, int fillColor) {
        int left = this.getRowLeft() - 2;
        int right = left + width + 4;
        graphics.fill(left, top - 2, right, top + height + 2, outlineColor);
        graphics.fill(left + 1, top - 1, right - 1, top + height + 1, fillColor);
    }

    /**
     * リストの各エントリ
     */
    public static class Entry extends ObjectSelectionList.Entry<WaypointListWidget.Entry> {

        private static final ResourceLocation TRUE_QUICK_ICON =
                new ResourceLocation(TokorotenSlimeAPI.getModId(), "textures/item/spacetime_eye.png");
        private static final ResourceLocation FALSE_QUICK_ICON =
                new ResourceLocation(TokorotenSlimeAPI.getModId(), "textures/gui/false_quick_icon.png");

        private final WaypointListWidget parent;
        public final WaypointData data;
        private final Minecraft minecraft;

        public Entry(WaypointListWidget parent, WaypointData data, Minecraft minecraft) {
            this.parent = parent;
            this.data = data;
            this.minecraft = minecraft;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            // ホバー時の背景描画(行の四辺を均等に2pxずつ広げた枠にする)
            int nx = left - 2;
            int ny = top - 2;
            int nw = width + 4;
            int nh = height + 4;

            if (hovered) {
                graphics.fill(nx, ny, nx + nw, ny + nh, 0x40FFFFFF);
                drawBorder(graphics, nx, ny, nw, nh, 0xFFFFFFFF);
            }

            UUID quick = WaypointClientCache.getQuick();

            // クイックアイコン描画
            graphics.blit(
                    data.uuid().equals(quick) ? TRUE_QUICK_ICON : FALSE_QUICK_ICON,
                    left + 4, top + 2, 0f, 0f, 16, 16, 16, 16
            );

            int textX = left + 29;

            // 名前描画(クイック指定は黄色)
            graphics.drawString(minecraft.font,
                    Component.literal(data.name()).withStyle(
                            data.uuid().equals(quick) ? ChatFormatting.YELLOW : ChatFormatting.WHITE
                    ),
                    textX, top + 1, 0xFFFFFF);

            // 座標描画
            graphics.drawString(minecraft.font,
                    Component.literal("XYZ: " + data.blockPos().getX() +
                            " / " + data.blockPos().getY() +
                            " / " + data.blockPos().getZ()).withStyle(ChatFormatting.GRAY),
                    textX, top + 11, 0xAAAAAA);
        }

        @Override
        public Component getNarration() {
            return Component.literal("list");
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
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
        private static void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
            graphics.fill(x, y, x + w, y + 1, color);                // 上
            graphics.fill(x, y + h, x + w, y + h - 1, color);    // 下
            graphics.fill(x, y, x + 1, y + h, color);                // 左
            graphics.fill(x + w - 1, y, x + w, y + h, color);    // 右
        }
    }
}
