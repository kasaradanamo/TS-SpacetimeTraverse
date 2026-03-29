package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.server.ServerWaypointManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record RegisterQuickC2SPacket(UUID dataUuid) implements CustomPayload {

    public static final Id<RegisterQuickC2SPacket> ID =
            new Id<>(Identifier.of(TSSpacetimeTraverse.MOD_ID, "register_quick"));

    public static final PacketCodec<RegistryByteBuf, RegisterQuickC2SPacket> CODEC =
            PacketCodec.tuple(
                    Uuids.PACKET_CODEC,
                    RegisterQuickC2SPacket::dataUuid,
                    RegisterQuickC2SPacket::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(UUID dataUuid) {
        ClientPlayNetworking.send(new RegisterQuickC2SPacket(dataUuid));
    }

    public static void receive(RegisterQuickC2SPacket packet, ServerPlayerEntity player) {
        ServerWaypointManager.setQuick(player, packet.dataUuid());
    }
}