package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.server.PortalHandler;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record PlacePortalC2SPacket(UUID waypointDataUuid) implements CustomPayload {

    public static final CustomPayload.Id<PlacePortalC2SPacket> ID =
            new CustomPayload.Id<>(Identifier.of(TSSpacetimeTraverse.MOD_ID, "place_portal"));

    public static final PacketCodec<RegistryByteBuf, PlacePortalC2SPacket> CODEC =
            PacketCodec.tuple(
                    Uuids.PACKET_CODEC,
                    PlacePortalC2SPacket::waypointDataUuid,
                    PlacePortalC2SPacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(UUID waypointDataUuid) {
        ClientPlayNetworking.send(new PlacePortalC2SPacket(waypointDataUuid));
    }

    public static void receive(PlacePortalC2SPacket packet, ServerPlayerEntity player) {
        PortalHandler.placePortal(packet.waypointDataUuid(), player);
    }
}