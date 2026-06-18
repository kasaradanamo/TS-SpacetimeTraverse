package net.kasara.ts_spacetime_traverse.network.packet.s2c;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.client.data.DimensionClientCache;
import net.kasara.ts_spacetime_traverse.util.DimensionBounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public record DimensionListS2CPacket(Map<Identifier, DimensionBounds> dimensions) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DimensionListS2CPacket> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(TSSpacetimeTraverse.MOD_ID, "dimension_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DimensionListS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, DimensionBounds.STREAM_CODEC),
                    DimensionListS2CPacket::dimensions,
                    DimensionListS2CPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void send(ServerPlayer player, Map<Identifier, DimensionBounds> dimensions) {
        ServerPlayNetworking.send(player, new DimensionListS2CPacket(dimensions));
    }

    public static void receive(DimensionListS2CPacket packet) {
        DimensionClientCache.setAll(packet.dimensions());
    }
}
