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

public record VanishPortalC2SPacket() implements CustomPayload {

    public static final CustomPayload.Id<VanishPortalC2SPacket> ID =
            new CustomPayload.Id<>(Identifier.of(TSSpacetimeTraverse.MOD_ID, "vanish_portal"));

    public static final PacketCodec<RegistryByteBuf, VanishPortalC2SPacket> CODEC =
            PacketCodec.of((packet, buf) -> {}, VanishPortalC2SPacket::read);

    private static VanishPortalC2SPacket read(RegistryByteBuf buf) {
        return new VanishPortalC2SPacket();
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send() {
        ClientPlayNetworking.send(new VanishPortalC2SPacket());
    }

    public void receive(ServerPlayerEntity player) {
        MinecraftServer server = player.getEntityWorld().getServer();
        server.execute(() -> ServerPortalHandler.vanishOwnedPortals(player));
    }
}
