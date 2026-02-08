package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.server.ServerPortalHandler;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;


public record PlacePortalC2SPacket(UUID waypointDataUuid) implements CustomPayload {

    public static final CustomPayload.Id<PlacePortalC2SPacket> ID =
            new CustomPayload.Id<>(Identifier.of(TSSpacetimeTraverse.MOD_ID, "place_portal"));

    public static final PacketCodec<RegistryByteBuf, PlacePortalC2SPacket> CODEC =
            PacketCodec.of(PlacePortalC2SPacket::write, PlacePortalC2SPacket::read);

    private static void write(PlacePortalC2SPacket packet, RegistryByteBuf buf) {
        buf.writeUuid(packet.waypointDataUuid());
    }

    private static PlacePortalC2SPacket read(RegistryByteBuf buf) {
        return new PlacePortalC2SPacket(buf.readUuid());
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(UUID waypointDataUuid) {
        ClientPlayNetworking.send(new PlacePortalC2SPacket(waypointDataUuid));
    }

    public static void receive(PlacePortalC2SPacket packet, ServerPlayerEntity player) {
        MinecraftServer server = player.getEntityWorld().getServer();
        server.execute(() -> ServerPortalHandler.placePortal(packet.waypointDataUuid(), player));
    }
}