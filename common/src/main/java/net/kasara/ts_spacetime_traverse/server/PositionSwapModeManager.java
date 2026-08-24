package net.kasara.ts_spacetime_traverse.server;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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
    public static String get(Player player) {
        CompoundTag nbt = TokorotenSlimeAPI.getAddonData(player, TSSpacetimeTraverseCommon.MOD_ID);
        return nbt.getString(POSITION_SWAP_MODE).orElse(NORMAL);
    }

    /**
     * プレイヤーのスワップモードを設定する
     */
    public static void set(Player player, String mode) {
        CompoundTag nbt = TokorotenSlimeAPI.getAddonData(player, TSSpacetimeTraverseCommon.MOD_ID);
        nbt.putString(POSITION_SWAP_MODE, mode);

        if(player instanceof ServerPlayer serverPlayer) {
            TokorotenSlimeAPI.writeAddonData(serverPlayer, TSSpacetimeTraverseCommon.MOD_ID, nbt);
        }
    }

    /**
     * モード切替
     */
    public static void toggle(Player player) {
        Component modeText;

        if (get(player).equals(ENHANCED)) {
            set(player, NORMAL);
            modeText = Component.translatable("mode.tokorotenslime.position_swap." + NORMAL).setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE));
        } else {
            set(player, ENHANCED);
            modeText = Component.translatable("mode.tokorotenslime.position_swap." + ENHANCED).setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE));
        }

        player.sendSystemMessage(Component.translatable("message.tokorotenslime.position_swap_mode", modeText));
    }
}