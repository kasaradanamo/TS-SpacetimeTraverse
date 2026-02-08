package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.server.ServerWaypointManager;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;


public record ApplyWaypointChangeC2SPacket(WaypointData waypointData, boolean delete) implements CustomPayload {

    public static final CustomPayload.Id<ApplyWaypointChangeC2SPacket> ID =
            new CustomPayload.Id<>(Identifier.of(TSSpacetimeTraverse.MOD_ID, "register_waypoint"));

    public static final PacketCodec<RegistryByteBuf, ApplyWaypointChangeC2SPacket> CODEC =
            PacketCodec.of(ApplyWaypointChangeC2SPacket::write, ApplyWaypointChangeC2SPacket::read);

    private static void write(ApplyWaypointChangeC2SPacket packet, RegistryByteBuf buf) {
        packet.waypointData().write(buf);
        buf.writeBoolean(packet.delete());
    }

    private static ApplyWaypointChangeC2SPacket read(RegistryByteBuf buf) {
        return new ApplyWaypointChangeC2SPacket(WaypointData.read(buf), buf.readBoolean());
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(WaypointData waypointData, boolean delete) {
        ClientPlayNetworking.send(new ApplyWaypointChangeC2SPacket(waypointData, delete));
    }

    public static void receive(ApplyWaypointChangeC2SPacket packet, ServerPlayerEntity player) {
        MinecraftServer server = player.getEntityWorld().getServer();
        server.execute(() -> ServerWaypointManager.applyWaypointChange(player, packet.waypointData(), packet.delete()));
    }
}