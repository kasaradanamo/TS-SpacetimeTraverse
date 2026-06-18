package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.server.WaypointServerManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record RegisterQuickC2SPacket(UUID dataUuid) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RegisterQuickC2SPacket> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(TSSpacetimeTraverse.MOD_ID, "register_quick"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RegisterQuickC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    RegisterQuickC2SPacket::dataUuid,
                    RegisterQuickC2SPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void send(UUID dataUuid) {
        ClientPlayNetworking.send(new RegisterQuickC2SPacket(dataUuid));
    }

    public static void receive(RegisterQuickC2SPacket packet, ServerPlayer player) {
        WaypointServerManager.setQuick(player, packet.dataUuid());
    }
}