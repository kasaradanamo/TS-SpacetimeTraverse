package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.server.ServerWaypointManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record RegisterQuickC2SPacket(UUID dataUuid) implements CustomPayload {

    public static final CustomPayload.Id<RegisterQuickC2SPacket> ID =
            new CustomPayload.Id<>(Identifier.of(TSSpacetimeTraverse.MOD_ID, "register_quick"));

    public static final PacketCodec<RegistryByteBuf, RegisterQuickC2SPacket> CODEC =
            PacketCodec.of(RegisterQuickC2SPacket::write, RegisterQuickC2SPacket::read);

    private static void write(RegisterQuickC2SPacket packet, RegistryByteBuf buf) {
        buf.writeUuid(packet.dataUuid());
    }

    private static RegisterQuickC2SPacket read(RegistryByteBuf buf) {
        return new RegisterQuickC2SPacket(buf.readUuid());
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(UUID dataUuid) {
        ClientPlayNetworking.send(new RegisterQuickC2SPacket(dataUuid));
    }

    public static void receive(RegisterQuickC2SPacket packet, ServerPlayerEntity player) {
        MinecraftServer server = player.getEntityWorld().getServer();
        server.execute(() -> ServerWaypointManager.setQuick(player, packet.dataUuid()));
    }
}