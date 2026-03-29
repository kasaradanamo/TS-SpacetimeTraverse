package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.server.PositionSwapModeManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record PositionSwapModeC2SPacket() implements CustomPayload {

    public static final Id<PositionSwapModeC2SPacket> ID =
            new Id<>(Identifier.of(TSSpacetimeTraverse.MOD_ID, "position_swap_mode"));

    public static final PacketCodec<RegistryByteBuf, PositionSwapModeC2SPacket> CODEC =
            PacketCodec.unit(new PositionSwapModeC2SPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send() {
        ClientPlayNetworking.send(new PositionSwapModeC2SPacket());
    }

    public void receive(ServerPlayerEntity player) {
        PositionSwapModeManager.toggle(player);
    }
}
