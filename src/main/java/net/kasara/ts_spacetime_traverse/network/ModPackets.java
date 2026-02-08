package net.kasara.ts_spacetime_traverse.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.*;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.DimensionListS2CPacket;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.WaypointInfoS2CPacket;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModPackets {

    // PayloadTypesの登録
    public static void registerPayloadTypes() {
        registerPTC2S(TargetEntityIdC2SPacket.ID, TargetEntityIdC2SPacket.CODEC);
        registerPTC2S(ApplyWaypointChangeC2SPacket.ID, ApplyWaypointChangeC2SPacket.CODEC);
        registerPTC2S(RegisterQuickC2SPacket.ID, RegisterQuickC2SPacket.CODEC);
        registerPTC2S(PlacePortalC2SPacket.ID, PlacePortalC2SPacket.CODEC);
        registerPTC2S(VanishPortalC2SPacket.ID, VanishPortalC2SPacket.CODEC);

        registerPTS2C(WaypointInfoS2CPacket.ID, WaypointInfoS2CPacket.CODEC);
        registerPTS2C(DimensionListS2CPacket.ID, DimensionListS2CPacket.CODEC);

        // ログ
        TSSpacetimeTraverse.LOGGER.info("Registering addon PayloadTypes for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }

    // C2Sの登録
    public static void registerC2SPackets() {
        registerC2S(TargetEntityIdC2SPacket.ID, TargetEntityIdC2SPacket::receive);
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

    private static <T extends CustomPayload> void registerPTC2S(CustomPayload.Id<T> id, PacketCodec<RegistryByteBuf, T> codec) {
        PayloadTypeRegistry.playC2S().register(id, codec);
    }

    private static <T extends CustomPayload> void registerPTS2C(CustomPayload.Id<T> id, PacketCodec<RegistryByteBuf, T> codec) {
        PayloadTypeRegistry.playS2C().register(id, codec);
    }

    private static <T extends CustomPayload> void registerC2S(CustomPayload.Id<T> id, BiConsumer<T, ServerPlayerEntity> handler) {
        ServerPlayNetworking.registerGlobalReceiver(id, (packet, context) -> handler.accept(packet, context.player()));
    }

    private static <T extends CustomPayload> void registerS2C(CustomPayload.Id<T> id, Consumer<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(id, (packet, context) -> handler.accept(packet));
    }
}