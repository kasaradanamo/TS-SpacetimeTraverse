package net.kasara.ts_spacetime_traverse.client.gui;

import net.minecraft.client.gui.GuiGraphics;

public final class GuiBackgrounds {

    private static final int MENU_BACKGROUND = 0x90000000;

    private static final int LIST_BACKGROUND = 0x70000000;

    /**
     * 画面全体の背景を描く
     */
    public static void drawMenuBackground(GuiGraphics graphics, int width, int height) {
        graphics.fill(0, 0, width, height, MENU_BACKGROUND);
    }

    // inworld_header/footer_separator.png の各行の色(高さ2px)
    private static final int SEPARATOR_LIGHT = 0x33FFFFFF;
    private static final int SEPARATOR_DARK = 0xBF000000;

    /**
     * リスト領域の背景を描く(中身より先に呼ぶ)
     */
    public static void drawListBackground(GuiGraphics graphics, int left, int right, int top, int bottom) {
        graphics.fill(left, top, right, bottom, LIST_BACKGROUND);
    }

    /**
     * リスト領域の上端の外側と下端の外側に区切り線を描く
     */
    public static void drawListSeparators(GuiGraphics graphics, int left, int right, int top, int bottom) {
        graphics.fill(left, top - 2, right, top - 1, SEPARATOR_LIGHT);
        graphics.fill(left, top - 1, right, top, SEPARATOR_DARK);

        graphics.fill(left, bottom, right, bottom + 1, SEPARATOR_DARK);
        graphics.fill(left, bottom + 1, right, bottom + 2, SEPARATOR_LIGHT);
    }

    private GuiBackgrounds() {}
}
