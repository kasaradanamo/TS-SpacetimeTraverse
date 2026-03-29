package net.kasara.ts_spacetime_traverse.network.packet.s2c;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.client.data.DimensionClientCache;
import net.kasara.ts_spacetime_traverse.util.DimensionBounds;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

public record DimensionListS2CPacket(Map<Identifier, DimensionBounds> dimensions) implements CustomPayload {

    public static final CustomPayload.Id<DimensionListS2CPacket> ID =
            new CustomPayload.Id<>(Identifier.of(TSSpacetimeTraverse.MOD_ID, "dimension_list"));

    public static final PacketCodec<RegistryByteBuf, DimensionListS2CPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, DimensionBounds.PACKET_CODEC),
                    DimensionListS2CPacket::dimensions,
                    DimensionListS2CPacket::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(ServerPlayerEntity player, Map<Identifier, DimensionBounds> dimensions) {
        ServerPlayNetworking.send(player, new DimensionListS2CPacket(dimensions));
    }

    public static void receive(DimensionListS2CPacket packet) {
        DimensionClientCache.setAll(packet.dimensions());
    }
}
