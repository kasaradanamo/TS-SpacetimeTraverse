package net.kasara.ts_spacetime_traverse.server;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * プレイヤーに保存するスワップポジションモードの制御
 */
public class PositionSwapModeManager {

    // プレイヤーNBT内でポジションモードを保存するキー名
    private static final String POSITION_SWAP_MODE = "position_swap_mode";

    private static final String NORMAL = "normal";
    private static final String ENHANCED = "enhanced";

    /**
     * プレイヤーに保存されているスワップモードを取得
     * データが存在しない場合はnormalを返す
     */
    public static String get(PlayerEntity player) {
        NbtCompound nbt = TokorotenSlimeAPI.getAddonData(player, TSSpacetimeTraverse.MOD_ID);
        return nbt.getString(POSITION_SWAP_MODE).orElse(NORMAL);
    }

    /**
     * プレイヤーのスワップモードを設定する
     */
    public static void set(PlayerEntity player, String mode) {
        NbtCompound nbt = TokorotenSlimeAPI.getAddonData(player, TSSpacetimeTraverse.MOD_ID);
        nbt.putString(POSITION_SWAP_MODE, mode);

        if(player instanceof ServerPlayerEntity serverPlayer) {
            TokorotenSlimeAPI.writeAddonData(serverPlayer, TSSpacetimeTraverse.MOD_ID, nbt);
        }
    }

    /**
     * モード切替
     */
    public static void toggle(PlayerEntity player) {
        Text modeText;

        if (get(player).equals(ENHANCED)) {
            set(player, NORMAL);
            modeText = Text.translatable("mode.tokorotenslime.position_swap." + NORMAL).setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE));
        } else {
            set(player, ENHANCED);
            modeText = Text.translatable("mode.tokorotenslime.position_swap." + ENHANCED).setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE));
        }

        player.sendMessage(Text.translatable("message.tokorotenslime.position_swap_mode", modeText), false);
    }
}
