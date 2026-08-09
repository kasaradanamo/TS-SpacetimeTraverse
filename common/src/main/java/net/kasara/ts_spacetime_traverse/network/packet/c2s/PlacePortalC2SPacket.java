package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.server.PortalHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * ポータル設置をサーバーに要求する
 */
public record PlacePortalC2SPacket(UUID waypointDataUuid) {

    public static void encode(PlacePortalC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.waypointDataUuid());
    }

    public static PlacePortalC2SPacket decode(FriendlyByteBuf buf) {
        return new PlacePortalC2SPacket(buf.readUUID());
    }

    public static void send(UUID waypointDataUuid) {
        ModPacketsCommon.SEND_TO_SERVER.accept(new PlacePortalC2SPacket(waypointDataUuid));
    }

    public static void handle(PlacePortalC2SPacket packet, ServerPlayer sender) {
        if (sender != null) {
            PortalHandler.placePortal(packet.waypointDataUuid(), sender);
        }
    }
}
