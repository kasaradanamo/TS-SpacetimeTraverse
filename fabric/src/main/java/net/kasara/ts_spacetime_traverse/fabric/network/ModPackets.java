package net.kasara.ts_spacetime_traverse.fabric.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.ApplyWaypointChangeC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PlacePortalC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PositionSwapC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.PositionSwapModeC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.RegisterQuickC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.c2s.VanishPortalC2SPacket;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.DimensionListS2CPacket;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.WaypointInfoS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * TS_SpacetimeTraverseのネットワーキング(チャンネル登録とコンテキストアダプタ)。
 */
public class ModPackets {

    public static final ResourceLocation POSITION_SWAP = id("position_swap");
    public static final ResourceLocation POSITION_SWAP_MODE = id("position_swap_mode");
    public static final ResourceLocation APPLY_WAYPOINT_CHANGE = id("apply_waypoint_change");
    public static final ResourceLocation REGISTER_QUICK = id("register_quick");
    public static final ResourceLocation PLACE_PORTAL = id("place_portal");
    public static final ResourceLocation VANISH_PORTAL = id("vanish_portal");
    public static final ResourceLocation WAYPOINT_INFO = id("waypoint_info");
    public static final ResourceLocation DIMENSION_LIST = id("dimension_list");

    private static ResourceLocation id(String path) {
        return new ResourceLocation(TSSpacetimeTraverseCommon.MOD_ID, path);
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(POSITION_SWAP, (server, player, handler, buf, responseSender) -> {
            PositionSwapC2SPacket packet = PositionSwapC2SPacket.decode(buf);
            server.execute(() -> PositionSwapC2SPacket.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(POSITION_SWAP_MODE, (server, player, handler, buf, responseSender) -> {
            PositionSwapModeC2SPacket packet = PositionSwapModeC2SPacket.decode(buf);
            server.execute(() -> PositionSwapModeC2SPacket.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(APPLY_WAYPOINT_CHANGE, (server, player, handler, buf, responseSender) -> {
            ApplyWaypointChangeC2SPacket packet = ApplyWaypointChangeC2SPacket.decode(buf);
            server.execute(() -> ApplyWaypointChangeC2SPacket.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(REGISTER_QUICK, (server, player, handler, buf, responseSender) -> {
            RegisterQuickC2SPacket packet = RegisterQuickC2SPacket.decode(buf);
            server.execute(() -> RegisterQuickC2SPacket.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(PLACE_PORTAL, (server, player, handler, buf, responseSender) -> {
            PlacePortalC2SPacket packet = PlacePortalC2SPacket.decode(buf);
            server.execute(() -> PlacePortalC2SPacket.handle(packet, player));
        });
        ServerPlayNetworking.registerGlobalReceiver(VANISH_PORTAL, (server, player, handler, buf, responseSender) -> {
            VanishPortalC2SPacket packet = VanishPortalC2SPacket.decode(buf);
            server.execute(() -> VanishPortalC2SPacket.handle(packet, player));
        });

        // common側のsend()静的メソッドから呼ばれる送信ブリッジをセット
        ModPacketsCommon.SEND_TO_SERVER = ModPackets::sendToServer;
        ModPacketsCommon.SEND_TO_PLAYER = ModPackets::sendToPlayer;

        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Mod PayloadTypes for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        if (message instanceof WaypointInfoS2CPacket packet) {
            WaypointInfoS2CPacket.encode(packet, buf);
            ServerPlayNetworking.send(player, WAYPOINT_INFO, buf);
        } else if (message instanceof DimensionListS2CPacket packet) {
            DimensionListS2CPacket.encode(packet, buf);
            ServerPlayNetworking.send(player, DIMENSION_LIST, buf);
        }
    }

    // クライアント側からのsendToServer()はfabricモジュールのクライアント専用クラス(ClientModPackets)が担当する
    private static void sendToServer(Object message) {
        net.kasara.ts_spacetime_traverse.fabric.client.network.ClientModPackets.send(message);
    }
}
