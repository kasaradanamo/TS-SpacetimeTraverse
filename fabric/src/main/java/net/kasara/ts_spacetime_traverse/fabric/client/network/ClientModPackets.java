package net.kasara.ts_spacetime_traverse.fabric.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.kasara.ts_spacetime_traverse.fabric.network.ModPackets;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.ApplyWaypointChangeC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PlacePortalC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PositionSwapC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PositionSwapModeC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.RegisterQuickC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.VanishPortalC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.DimensionListS2CPacket;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.WaypointInfoS2CPacket;
import net.minecraft.network.FriendlyByteBuf;

/**
 * クライアント側専用のパケット送受信。
 */
public final class ClientModPackets {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.WAYPOINT_INFO, (client, handler, buf, responseSender) -> {
            WaypointInfoS2CPacket packet = WaypointInfoS2CPacket.decode(buf);
            client.execute(() -> WaypointInfoS2CPacket.handle(packet));
        });
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.DIMENSION_LIST, (client, handler, buf, responseSender) -> {
            DimensionListS2CPacket packet = DimensionListS2CPacket.decode(buf);
            client.execute(() -> DimensionListS2CPacket.handle(packet));
        });
    }

    public static void send(Object message) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        if (message instanceof PositionSwapC2SPacket packet) {
            PositionSwapC2SPacket.encode(packet, buf);
            ClientPlayNetworking.send(ModPackets.POSITION_SWAP, buf);
        } else if (message instanceof PositionSwapModeC2SPacket packet) {
            PositionSwapModeC2SPacket.encode(packet, buf);
            ClientPlayNetworking.send(ModPackets.POSITION_SWAP_MODE, buf);
        } else if (message instanceof ApplyWaypointChangeC2SPacket packet) {
            ApplyWaypointChangeC2SPacket.encode(packet, buf);
            ClientPlayNetworking.send(ModPackets.APPLY_WAYPOINT_CHANGE, buf);
        } else if (message instanceof RegisterQuickC2SPacket packet) {
            RegisterQuickC2SPacket.encode(packet, buf);
            ClientPlayNetworking.send(ModPackets.REGISTER_QUICK, buf);
        } else if (message instanceof PlacePortalC2SPacket packet) {
            PlacePortalC2SPacket.encode(packet, buf);
            ClientPlayNetworking.send(ModPackets.PLACE_PORTAL, buf);
        } else if (message instanceof VanishPortalC2SPacket packet) {
            VanishPortalC2SPacket.encode(packet, buf);
            ClientPlayNetworking.send(ModPackets.VANISH_PORTAL, buf);
        }
    }

    private ClientModPackets() {}
}
