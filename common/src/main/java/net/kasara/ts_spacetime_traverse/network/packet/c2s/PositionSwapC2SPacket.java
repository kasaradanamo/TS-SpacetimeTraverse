package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.server.PositionSwapServerHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public record PositionSwapC2SPacket(int targetId, ResourceKey<Level> dimension, boolean random) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PositionSwapC2SPacket> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(TSSpacetimeTraverseCommon.MOD_ID, "position_swap"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PositionSwapC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    PositionSwapC2SPacket::targetId,
                    ResourceKey.streamCodec(Registries.DIMENSION),
                    PositionSwapC2SPacket::dimension,
                    ByteBufCodecs.BOOL,
                    PositionSwapC2SPacket::random,
                    PositionSwapC2SPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void send(int targetId, ResourceKey<Level> dimension, boolean random) {
        ModPacketsCommon.sendToServer(new PositionSwapC2SPacket(targetId, dimension, random));
    }

    public static void receive(PositionSwapC2SPacket packet, ServerPlayer player) {
        PositionSwapServerHandler.positionSwap(packet.targetId(), packet.dimension(), player, packet.random());
    }
}