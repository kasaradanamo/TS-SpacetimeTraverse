package net.kasara.ts_spacetime_traverse.neoforge.network;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.neoforge.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.ApplyWaypointChangeC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PlacePortalC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PositionSwapC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PositionSwapModeC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.RegisterQuickC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.VanishPortalC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.DimensionListS2CPacket;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.WaypointInfoS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPackets {

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // c2s (受信ハンドラのplayer()はサーバーバウンドではServerPlayerを返す)
        registrar.playToServer(PositionSwapC2SPacket.ID, PositionSwapC2SPacket.STREAM_CODEC,
                (packet, context) -> PositionSwapC2SPacket.receive(packet, (ServerPlayer) context.player()));
        registrar.playToServer(PositionSwapModeC2SPacket.ID, PositionSwapModeC2SPacket.STREAM_CODEC,
                (packet, context) -> packet.receive((ServerPlayer) context.player()));
        registrar.playToServer(ApplyWaypointChangeC2SPacket.ID, ApplyWaypointChangeC2SPacket.STREAM_CODEC,
                (packet, context) -> ApplyWaypointChangeC2SPacket.receive(packet, (ServerPlayer) context.player()));
        registrar.playToServer(RegisterQuickC2SPacket.ID, RegisterQuickC2SPacket.STREAM_CODEC,
                (packet, context) -> RegisterQuickC2SPacket.receive(packet, (ServerPlayer) context.player()));
        registrar.playToServer(PlacePortalC2SPacket.ID, PlacePortalC2SPacket.STREAM_CODEC,
                (packet, context) -> PlacePortalC2SPacket.receive(packet, (ServerPlayer) context.player()));
        registrar.playToServer(VanishPortalC2SPacket.ID, VanishPortalC2SPacket.STREAM_CODEC,
                (packet, context) -> packet.receive((ServerPlayer) context.player()));

        // s2c
        registrar.playToClient(WaypointInfoS2CPacket.ID, WaypointInfoS2CPacket.STREAM_CODEC,
                (packet, context) -> WaypointInfoS2CPacket.receive(packet));
        registrar.playToClient(DimensionListS2CPacket.ID, DimensionListS2CPacket.STREAM_CODEC,
                (packet, context) -> DimensionListS2CPacket.receive(packet));
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModPackets::onRegisterPayloads);

        // ログ
        TSSpacetimeTraverse.LOGGER.info("Registering addon PayloadTypes for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        player.connection.send(new ClientboundCustomPayloadPacket(payload));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        if (Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(payload));
        }
    }
}
