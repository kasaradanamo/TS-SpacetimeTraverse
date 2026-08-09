package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.server.PositionSwapModeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * ポジションスワップのモード切替をサーバーに通知する
 */
public record PositionSwapModeC2SPacket() {

    public static void encode(PositionSwapModeC2SPacket packet, FriendlyByteBuf buf) {
        // フィールドなし
    }

    public static PositionSwapModeC2SPacket decode(FriendlyByteBuf buf) {
        return new PositionSwapModeC2SPacket();
    }

    public static void send() {
        ModPacketsCommon.SEND_TO_SERVER.accept(new PositionSwapModeC2SPacket());
    }

    public static void handle(PositionSwapModeC2SPacket packet, ServerPlayer sender) {
        if (sender != null) {
            PositionSwapModeManager.toggle(sender);
        }
    }
}
