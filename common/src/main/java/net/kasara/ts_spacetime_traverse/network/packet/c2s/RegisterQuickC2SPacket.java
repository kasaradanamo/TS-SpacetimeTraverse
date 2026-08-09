package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.server.WaypointServerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * クイックウェイポイントの登録をサーバーに要求する
 */
public record RegisterQuickC2SPacket(UUID dataUuid) {

    public static void encode(RegisterQuickC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.dataUuid());
    }

    public static RegisterQuickC2SPacket decode(FriendlyByteBuf buf) {
        return new RegisterQuickC2SPacket(buf.readUUID());
    }

    public static void send(UUID dataUuid) {
        ModPacketsCommon.SEND_TO_SERVER.accept(new RegisterQuickC2SPacket(dataUuid));
    }

    public static void handle(RegisterQuickC2SPacket packet, ServerPlayer sender) {
        if (sender != null) {
            WaypointServerManager.setQuick(sender, packet.dataUuid());
        }
    }
}
