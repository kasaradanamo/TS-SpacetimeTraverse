package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.server.PositionSwapServerHandler;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public record PositionSwapC2SPacket(int targetId, RegistryKey<World> dimension, boolean random) implements CustomPayload {

    public static final CustomPayload.Id<PositionSwapC2SPacket> ID =
            new CustomPayload.Id<>(Identifier.of(TSSpacetimeTraverse.MOD_ID, "position_swap"));

    public static final PacketCodec<RegistryByteBuf, PositionSwapC2SPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER,
                    PositionSwapC2SPacket::targetId,
                    RegistryKey.createPacketCodec(RegistryKeys.WORLD),
                    PositionSwapC2SPacket::dimension,
                    PacketCodecs.BOOLEAN,
                    PositionSwapC2SPacket::random,
                    PositionSwapC2SPacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(int targetId, RegistryKey<World> dimension, boolean random) {
        ClientPlayNetworking.send(new PositionSwapC2SPacket(targetId, dimension, random));
    }

    public static void receive(PositionSwapC2SPacket packet, ServerPlayerEntity player) {
        PositionSwapServerHandler.positionSwap(packet.targetId(), packet.dimension(), player, packet.random());
    }
}