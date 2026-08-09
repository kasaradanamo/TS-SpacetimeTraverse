package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.server.WaypointServerManager;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * ウェイポイントの追加・更新・削除をサーバーに要求する
 */
public record ApplyWaypointChangeC2SPacket(WaypointData waypointData, boolean delete) {

    public static void encode(ApplyWaypointChangeC2SPacket packet, FriendlyByteBuf buf) {
        WaypointData.encode(packet.waypointData(), buf);
        buf.writeBoolean(packet.delete());
    }

    public static ApplyWaypointChangeC2SPacket decode(FriendlyByteBuf buf) {
        return new ApplyWaypointChangeC2SPacket(WaypointData.decode(buf), buf.readBoolean());
    }

    public static void send(WaypointData waypointData, boolean delete) {
        ModPacketsCommon.SEND_TO_SERVER.accept(new ApplyWaypointChangeC2SPacket(waypointData, delete));
    }

    public static void handle(ApplyWaypointChangeC2SPacket packet, ServerPlayer sender) {
        if (sender != null) {
            WaypointServerManager.applyWaypointChange(sender, packet.waypointData(), packet.delete());
        }
    }
}
