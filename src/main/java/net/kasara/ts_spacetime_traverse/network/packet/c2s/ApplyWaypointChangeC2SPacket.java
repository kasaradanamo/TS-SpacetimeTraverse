package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.server.WaypointServerManager;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record ApplyWaypointChangeC2SPacket(WaypointData waypointData, boolean delete) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ApplyWaypointChangeC2SPacket> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(TSSpacetimeTraverse.MOD_ID, "register_waypoint"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ApplyWaypointChangeC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    WaypointData.STREAM_CODEC,
                    ApplyWaypointChangeC2SPacket::waypointData,
                    ByteBufCodecs.BOOL,
                    ApplyWaypointChangeC2SPacket::delete,
                    ApplyWaypointChangeC2SPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void send(WaypointData waypointData, boolean delete) {
        ClientPlayNetworking.send(new ApplyWaypointChangeC2SPacket(waypointData, delete));
    }

    public static void receive(ApplyWaypointChangeC2SPacket packet, ServerPlayer player) {
        WaypointServerManager.applyWaypointChange(player, packet.waypointData(), packet.delete());
    }
}