package net.kasara.ts_spacetime_traverse.fabric.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.fabric.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.*;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.DimensionListS2CPacket;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.WaypointInfoS2CPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModPackets {

    // PayloadTypesの登録
    public static void registerPayloadTypes() {
        registerPTC2S(PositionSwapC2SPacket.ID, PositionSwapC2SPacket.STREAM_CODEC);
        registerPTC2S(PositionSwapModeC2SPacket.ID, PositionSwapModeC2SPacket.STREAM_CODEC);
        registerPTC2S(ApplyWaypointChangeC2SPacket.ID, ApplyWaypointChangeC2SPacket.STREAM_CODEC);
        registerPTC2S(RegisterQuickC2SPacket.ID, RegisterQuickC2SPacket.STREAM_CODEC);
        registerPTC2S(PlacePortalC2SPacket.ID, PlacePortalC2SPacket.STREAM_CODEC);
        registerPTC2S(VanishPortalC2SPacket.ID, VanishPortalC2SPacket.STREAM_CODEC);

        registerPTS2C(WaypointInfoS2CPacket.ID, WaypointInfoS2CPacket.STREAM_CODEC);
        registerPTS2C(DimensionListS2CPacket.ID, DimensionListS2CPacket.STREAM_CODEC);

        // ログ
        TSSpacetimeTraverse.LOGGER.info("Registering addon PayloadTypes for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }

    // C2Sの登録
    public static void registerC2SPackets() {
        registerC2S(PositionSwapC2SPacket.ID, PositionSwapC2SPacket::receive);
        registerC2S(PositionSwapModeC2SPacket.ID, PositionSwapModeC2SPacket::receive);
        registerC2S(ApplyWaypointChangeC2SPacket.ID, ApplyWaypointChangeC2SPacket::receive);
        registerC2S(RegisterQuickC2SPacket.ID, RegisterQuickC2SPacket::receive);
        registerC2S(PlacePortalC2SPacket.ID, PlacePortalC2SPacket::receive);
        registerC2S(VanishPortalC2SPacket.ID, VanishPortalC2SPacket::receive);

        // ログ
        TSSpacetimeTraverse.LOGGER.info("Registering addon C2SPackets for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }

    // S2Cの登録
    public static void registerS2CPackets() {
        registerS2C(WaypointInfoS2CPacket.ID, WaypointInfoS2CPacket::receive);
        registerS2C(DimensionListS2CPacket.ID, DimensionListS2CPacket::receive);

        // ログ
        TSSpacetimeTraverse.LOGGER.info("Registering addon S2CPackets for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }

    private static <T extends CustomPacketPayload> void registerPTC2S(CustomPacketPayload.Type<T> id, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.serverboundPlay().register(id, codec);
    }

    private static <T extends CustomPacketPayload> void registerPTS2C(CustomPacketPayload.Type<T> id, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.clientboundPlay().register(id, codec);
    }

    private static <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> id, BiConsumer<T, ServerPlayer> handler) {
        ServerPlayNetworking.registerGlobalReceiver(id, (packet, context) -> handler.accept(packet, context.player()));
    }

    private static <T extends CustomPacketPayload> void registerS2C(CustomPacketPayload.Type<T> id, Consumer<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(id, (packet, context) -> handler.accept(packet));
    }
}
