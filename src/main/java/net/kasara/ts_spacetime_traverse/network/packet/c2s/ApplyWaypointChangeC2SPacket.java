package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.server.WaypointServerManager;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record ApplyWaypointChangeC2SPacket(WaypointData waypointData, boolean delete) implements CustomPayload {

    public static final Id<ApplyWaypointChangeC2SPacket> ID =
            new Id<>(Identifier.of(TSSpacetimeTraverse.MOD_ID, "register_waypoint"));

    public static final PacketCodec<RegistryByteBuf, ApplyWaypointChangeC2SPacket> CODEC =
            PacketCodec.tuple(
                    WaypointData.PACKET_CODEC,
                    ApplyWaypointChangeC2SPacket::waypointData,
                    PacketCodecs.BOOLEAN,
                    ApplyWaypointChangeC2SPacket::delete,
                    ApplyWaypointChangeC2SPacket::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(WaypointData waypointData, boolean delete) {
        ClientPlayNetworking.send(new ApplyWaypointChangeC2SPacket(waypointData, delete));
    }

    public static void receive(ApplyWaypointChangeC2SPacket packet, ServerPlayerEntity player) {
        WaypointServerManager.applyWaypointChange(player, packet.waypointData(), packet.delete());
    }
}