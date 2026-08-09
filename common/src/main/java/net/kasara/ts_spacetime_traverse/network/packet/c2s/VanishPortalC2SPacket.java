package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.server.PortalHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * 所有ポータルの消滅をサーバーに要求する
 */
public record VanishPortalC2SPacket() {

    public static void encode(VanishPortalC2SPacket packet, FriendlyByteBuf buf) {
        // フィールドなし
    }

    public static VanishPortalC2SPacket decode(FriendlyByteBuf buf) {
        return new VanishPortalC2SPacket();
    }

    public static void send() {
        ModPacketsCommon.SEND_TO_SERVER.accept(new VanishPortalC2SPacket());
    }

    public static void handle(VanishPortalC2SPacket packet, ServerPlayer sender) {
        if (sender != null) {
            PortalHandler.vanishOwnedPortals(sender);
        }
    }
}
