package net.kasara.ts_spacetime_traverse.network.packet.s2c;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.client.WaypointClientManager;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record WaypointInfoS2CPacket(List<WaypointData> waypoints, UUID quick) implements CustomPayload {

    public static final Id<WaypointInfoS2CPacket> ID =
            new Id<>(Identifier.of(TokorotenSlimeAPI.getModId(), "waypoints_info"));

    public static final PacketCodec<RegistryByteBuf, WaypointInfoS2CPacket> CODEC =
            PacketCodec.of(WaypointInfoS2CPacket::write, buf -> {
                List<WaypointData> waypoints = readWaypoints(buf);
                UUID quick = buf.readBoolean() ? buf.readUuid() : null;
                return new WaypointInfoS2CPacket(waypoints, quick);
            });

    private static void write(WaypointInfoS2CPacket packet, RegistryByteBuf buf) {
        buf.writeInt(packet.waypoints.size());
        for (WaypointData wp : packet.waypoints) wp.write(buf);

        buf.writeBoolean(packet.quick() != null);

        if (packet.quick() != null) buf.writeUuid(packet.quick());
    }

    private static List<WaypointData> readWaypoints(RegistryByteBuf buf) {
        int size = buf.readInt();
        List<WaypointData> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) list.add(WaypointData.read(buf));
        return list;
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(ServerPlayerEntity player, Collection<WaypointData> waypoints, UUID quick) {
        ServerPlayNetworking.send(player, new WaypointInfoS2CPacket(new ArrayList<>(waypoints), quick));
    }

    public static void receive(WaypointInfoS2CPacket packet) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            WaypointClientManager.waypointInfo(packet.waypoints(), packet.quick());
        });
    }
}
