package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.server.PositionSwapModeManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record PositionSwapModeC2SPacket() implements CustomPacketPayload{

    public static final CustomPacketPayload.Type<PositionSwapModeC2SPacket> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(TSSpacetimeTraverse.MOD_ID, "position_swap_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PositionSwapModeC2SPacket> STREAM_CODEC =
            StreamCodec.unit(new PositionSwapModeC2SPacket());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void send() {
        ClientPlayNetworking.send(new PositionSwapModeC2SPacket());
    }

    public void receive(ServerPlayer player) {
        PositionSwapModeManager.toggle(player);
    }
}