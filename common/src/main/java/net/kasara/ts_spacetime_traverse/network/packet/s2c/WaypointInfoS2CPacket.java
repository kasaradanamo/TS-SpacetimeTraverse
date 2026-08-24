package net.kasara.ts_spacetime_traverse.network.packet.s2c;

import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.client.WaypointClientManager;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public record WaypointInfoS2CPacket(List<WaypointData> waypoints, UUID quick) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WaypointInfoS2CPacket> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(TSSpacetimeTraverseCommon.MOD_ID, "waypoints_info"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WaypointInfoS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, WaypointData.STREAM_CODEC),
                    WaypointInfoS2CPacket::waypoints,
                    ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
                    packet -> Optional.ofNullable(packet.quick()),
                    (waypoints, quickOpt) -> new WaypointInfoS2CPacket(
                            waypoints,
                            quickOpt.orElse(null)
                    )
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void send(ServerPlayer player, Collection<WaypointData> waypoints, UUID quick) {
        ModPacketsCommon.sendToPlayer(player, new WaypointInfoS2CPacket(new ArrayList<>(waypoints), quick));
    }

    public static void receive(WaypointInfoS2CPacket packet) {
        WaypointClientManager.waypointInfo(packet.waypoints(), packet.quick());
    }
}
