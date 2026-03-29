package net.kasara.ts_spacetime_traverse.network.packet.s2c;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.client.WaypointClientManager;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.*;

public record WaypointInfoS2CPacket(List<WaypointData> waypoints, UUID quick) implements CustomPayload {

    public static final CustomPayload.Id<WaypointInfoS2CPacket> ID =
            new CustomPayload.Id<>(Identifier.of(TSSpacetimeTraverse.MOD_ID, "waypoints_info"));

    public static final PacketCodec<RegistryByteBuf, WaypointInfoS2CPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.collection(ArrayList::new, WaypointData.PACKET_CODEC),
                    WaypointInfoS2CPacket::waypoints,
                    PacketCodecs.optional(Uuids.PACKET_CODEC),
                    p -> Optional.ofNullable(p.quick()),
                    (waypoints, quick) -> new WaypointInfoS2CPacket(
                            waypoints,
                            quick.orElse(null)
                    )
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(ServerPlayerEntity player, Collection<WaypointData> waypoints, UUID quick) {
        ServerPlayNetworking.send(player, new WaypointInfoS2CPacket(new ArrayList<>(waypoints), quick));
    }

    public static void receive(WaypointInfoS2CPacket packet) {
        WaypointClientManager.waypointInfo(packet.waypoints(), packet.quick());
    }
}
