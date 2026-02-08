package net.kasara.ts_spacetime_traverse.network.packet.s2c;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.client.data.DimensionClientCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

public record DimensionListS2CPacket(Set<Identifier> dimensions) implements CustomPayload {

    public static final Id<DimensionListS2CPacket> ID =
            new Id<>(Identifier.of(TokorotenSlimeAPI.getModId(), "dimension_list"));

    public static final PacketCodec<RegistryByteBuf, DimensionListS2CPacket> CODEC =
            PacketCodec.of(DimensionListS2CPacket::write, DimensionListS2CPacket::read);

    private static void write(DimensionListS2CPacket packet, RegistryByteBuf buf) {
        Set<Identifier> set = packet.dimensions();
        buf.writeVarInt(set.size());
        for (Identifier id : set) {
            buf.writeIdentifier(id);
        }
    }

    private static DimensionListS2CPacket read(RegistryByteBuf buf) {
        int size = buf.readVarInt();
        Set<Identifier> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(buf.readIdentifier());
        }
        return new DimensionListS2CPacket(set);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(ServerPlayerEntity player, Set<Identifier> dimensions) {
        ServerPlayNetworking.send(player, new DimensionListS2CPacket(new HashSet<>(dimensions)));
    }

    public static void receive(DimensionListS2CPacket packet) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            DimensionClientCache.setAll(packet.dimensions());
        });
    }
}
