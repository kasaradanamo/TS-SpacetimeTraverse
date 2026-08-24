package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.server.PortalHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record PlacePortalC2SPacket(UUID waypointDataUuid) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlacePortalC2SPacket> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(TSSpacetimeTraverseCommon.MOD_ID, "place_portal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlacePortalC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    PlacePortalC2SPacket::waypointDataUuid,
                    PlacePortalC2SPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void send(UUID waypointDataUuid) {
        ModPacketsCommon.sendToServer(new PlacePortalC2SPacket(waypointDataUuid));
    }

    public static void receive(PlacePortalC2SPacket packet, ServerPlayer player) {
        PortalHandler.placePortal(packet.waypointDataUuid(), player);
    }
}