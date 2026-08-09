package net.kasara.ts_spacetime_traverse.forge.network;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * TS_SpacetimeTraverseのネットワーキング({@link SimpleChannel}のチャンネル登録とコンテキストアダプタ)。
 */
public class ModPackets {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TSSpacetimeTraverseCommon.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int nextId() {
        return packetId++;
    }

    public static void register() {
        // C2S
        CHANNEL.registerMessage(nextId(), PositionSwapC2SPacket.class,
                PositionSwapC2SPacket::encode, PositionSwapC2SPacket::decode, ModPackets::handlePositionSwap);
        CHANNEL.registerMessage(nextId(), PositionSwapModeC2SPacket.class,
                PositionSwapModeC2SPacket::encode, PositionSwapModeC2SPacket::decode, ModPackets::handlePositionSwapMode);
        CHANNEL.registerMessage(nextId(), ApplyWaypointChangeC2SPacket.class,
                ApplyWaypointChangeC2SPacket::encode, ApplyWaypointChangeC2SPacket::decode, ModPackets::handleApplyWaypointChange);
        CHANNEL.registerMessage(nextId(), RegisterQuickC2SPacket.class,
                RegisterQuickC2SPacket::encode, RegisterQuickC2SPacket::decode, ModPackets::handleRegisterQuick);
        CHANNEL.registerMessage(nextId(), PlacePortalC2SPacket.class,
                PlacePortalC2SPacket::encode, PlacePortalC2SPacket::decode, ModPackets::handlePlacePortal);
        CHANNEL.registerMessage(nextId(), VanishPortalC2SPacket.class,
                VanishPortalC2SPacket::encode, VanishPortalC2SPacket::decode, ModPackets::handleVanishPortal);

        // S2C
        CHANNEL.registerMessage(nextId(), WaypointInfoS2CPacket.class,
                WaypointInfoS2CPacket::encode, WaypointInfoS2CPacket::decode, ModPackets::handleWaypointInfo);
        CHANNEL.registerMessage(nextId(), DimensionListS2CPacket.class,
                DimensionListS2CPacket::encode, DimensionListS2CPacket::decode, ModPackets::handleDimensionList);

        ModPacketsCommon.SEND_TO_SERVER = ModPackets::sendToServer;
        ModPacketsCommon.SEND_TO_PLAYER = ModPackets::sendToPlayer;

        // ログ
        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Mod PayloadTypes for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }

    private static void handlePositionSwap(PositionSwapC2SPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> PositionSwapC2SPacket.handle(packet, ctx.getSender()));
        ctx.setPacketHandled(true);
    }

    private static void handlePositionSwapMode(PositionSwapModeC2SPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> PositionSwapModeC2SPacket.handle(packet, ctx.getSender()));
        ctx.setPacketHandled(true);
    }

    private static void handleApplyWaypointChange(ApplyWaypointChangeC2SPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ApplyWaypointChangeC2SPacket.handle(packet, ctx.getSender()));
        ctx.setPacketHandled(true);
    }

    private static void handleRegisterQuick(RegisterQuickC2SPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> RegisterQuickC2SPacket.handle(packet, ctx.getSender()));
        ctx.setPacketHandled(true);
    }

    private static void handlePlacePortal(PlacePortalC2SPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> PlacePortalC2SPacket.handle(packet, ctx.getSender()));
        ctx.setPacketHandled(true);
    }

    private static void handleVanishPortal(VanishPortalC2SPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> VanishPortalC2SPacket.handle(packet, ctx.getSender()));
        ctx.setPacketHandled(true);
    }

    private static void handleWaypointInfo(WaypointInfoS2CPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> WaypointInfoS2CPacket.handle(packet));
        ctx.setPacketHandled(true);
    }

    private static void handleDimensionList(DimensionListS2CPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DimensionListS2CPacket.handle(packet));
        ctx.setPacketHandled(true);
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }
}
