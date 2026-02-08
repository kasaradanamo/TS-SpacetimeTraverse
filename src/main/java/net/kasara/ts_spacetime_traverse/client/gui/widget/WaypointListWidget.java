package net.kasara.ts_spacetime_traverse.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.client.data.WaypointClientCache;
import net.kasara.ts_spacetime_traverse.client.gui.screen.PortalActionScreen;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * PortalActionScreen内のウェイポイントリスト
 */
@Environment(EnvType.CLIENT)
public class WaypointListWidget extends AlwaysSelectedEntryListWidget<WaypointListWidget.Entry> {

    /** このリストを持つScreen*/
    public final PortalActionScreen parentScreen;

    public WaypointListWidget(MinecraftClient client, PortalActionScreen screen) {
        super(client, screen.width, screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), 24);
        this.centerListVertically = false;
        this.parentScreen = screen;

        // ClientWaypointManagerから全ウェイポイントを取得してEntryを追加
        WaypointClientCache.getAll().forEach(data -> this.addEntry(new Entry(this, data, client)));
    }

    /**
     * リストの各エントリ
     */
    @Environment(EnvType.CLIENT)
    public static class Entry extends AlwaysSelectedEntryListWidget.Entry<WaypointListWidget.Entry> {

        private static final Identifier TRUE_QUICK_ICON =
                Identifier.of(TokorotenSlimeAPI.getModId(), "textures/item/spacetime_eye.png");
        private static final Identifier FALSE_QUICK_ICON =
                Identifier.of(TokorotenSlimeAPI.getModId(), "textures/gui/false_quick_icon.png");

        private final WaypointListWidget parent;
        public final WaypointData data;
        private final TextWidget nameWidget;
        private final TextWidget dimensionWidget;
        private final TextWidget positionWidget;

        public Entry(WaypointListWidget parent, WaypointData data, MinecraftClient client) {
            this.parent = parent;
            this.data = data;

            // 名前、ディメンション、座標のテキストウィジェットを作成
            this.nameWidget = new TextWidget(Text.literal(data.name()).formatted(Formatting.WHITE), client.textRenderer);
            this.dimensionWidget = new TextWidget(Text.literal(data.dimension().getValue().toString()).formatted(Formatting.GRAY), client.textRenderer);
            this.positionWidget = new TextWidget(
                    Text.literal("XYZ: " + data.blockPos().getX() +
                            " / " + data.blockPos().getY() +
                            " / " + data.blockPos().getZ()).formatted(Formatting.GRAY),
                    client.textRenderer
            );
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int x = this.getContentX();
            int y = this.getContentY();
            int w = this.getWidth();
            int h = this.getHeight();

            int nx = x - 2;
            int ny = y - 2;

            // ホバー時の背景描画
            if (hovered) {
                context.fill(nx, ny, nx + w , ny + h, 0x40FFFFFF);
                drawBorder(context, nx, ny, w, h, 0xFFFFFFFF);
            }

            UUID quick = WaypointClientCache.getQuick();

            // クイックアイコン描画
            context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    data.uuid().equals(quick) ? TRUE_QUICK_ICON : FALSE_QUICK_ICON,
                    x + 4, y + 2, 0, 0, 16, 16, 16, 16
            );

            int textX = x + 29;

            // 名前描画
            this.nameWidget.setPosition(textX, y + 1);
            this.nameWidget.setMessage(Text.literal(data.name()).formatted(
                    data.uuid().equals(quick) ? Formatting.YELLOW : Formatting.WHITE
            ));
            this.nameWidget.render(context, mouseX, mouseY, deltaTicks);

            // 座標描画
            this.positionWidget.setPosition(textX, y + 11);
            this.positionWidget.render(context, mouseX, mouseY, deltaTicks);

            int maxWidth = getWidth() - 30; // ざっくり
            this.nameWidget.setMaxWidth(maxWidth);
            this.dimensionWidget.setMaxWidth(maxWidth);
            this.positionWidget.setMaxWidth(maxWidth);
        }

        @Override
        public Text getNarration() {
            return Text.literal("list");
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            if (click.button() == 0) {
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
        private static void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
            context.fill(x, y, x + w, y + 1, color);                // 上
            context.fill(x, y + h, x + w, y + h - 1, color);    // 下
            context.fill(x, y, x + 1, y + h, color);                // 左
            context.fill(x + w - 1, y, x + w, y + h, color);    // 右
        }
    }
}
