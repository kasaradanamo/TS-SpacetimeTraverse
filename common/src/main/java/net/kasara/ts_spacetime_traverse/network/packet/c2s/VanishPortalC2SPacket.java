package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.server.PortalHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record VanishPortalC2SPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<VanishPortalC2SPacket> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(TSSpacetimeTraverseCommon.MOD_ID, "vanish_portal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VanishPortalC2SPacket> STREAM_CODEC =
            StreamCodec.unit(new VanishPortalC2SPacket());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void send() {
        ModPacketsCommon.sendToServer(new VanishPortalC2SPacket());
    }

    public void receive(ServerPlayer player) {
        PortalHandler.vanishOwnedPortals(player);
    }
}
