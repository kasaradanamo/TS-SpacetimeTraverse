package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.server.SwapPositionHandler;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;


public record TargetEntityIdC2SPacket(int targetId, String dimensionName, boolean random) implements CustomPayload {

    public static final CustomPayload.Id<TargetEntityIdC2SPacket> ID =
            new CustomPayload.Id<>(Identifier.of(TSSpacetimeTraverse.MOD_ID, "target_entity"));

    public static final PacketCodec<RegistryByteBuf, TargetEntityIdC2SPacket> CODEC =
            PacketCodec.of(TargetEntityIdC2SPacket::write, TargetEntityIdC2SPacket::read);

    private static void write(TargetEntityIdC2SPacket packet, RegistryByteBuf buf) {
        buf.writeInt(packet.targetId());
        buf.writeString(packet.dimensionName());
        buf.writeBoolean(packet.random());
    }

    private static TargetEntityIdC2SPacket read(RegistryByteBuf buf) {
        return new TargetEntityIdC2SPacket(buf.readInt(), buf.readString(), buf.readBoolean());
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void send(int targetId, String dimensionName, boolean random) {
        ClientPlayNetworking.send(new TargetEntityIdC2SPacket(targetId, dimensionName, random));
    }

    public static void receive(TargetEntityIdC2SPacket packet, ServerPlayerEntity player) {
        MinecraftServer server = player.getEntityWorld().getServer();
        server.execute(() -> SwapPositionHandler.swapPosition(packet.targetId(), packet.dimensionName(), player, packet.random()));
    }
}