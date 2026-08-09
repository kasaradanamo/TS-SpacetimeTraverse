package net.kasara.ts_spacetime_traverse.network.packet.s2c;

import net.kasara.ts_spacetime_traverse.client.data.DimensionClientCache;
import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.util.DimensionBounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * 全ディメンションの境界情報をクライアントに送信する
 */
public record DimensionListS2CPacket(Map<ResourceLocation, DimensionBounds> dimensions) {

    public static void encode(DimensionListS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeMap(packet.dimensions(),
                (b, id) -> ((FriendlyByteBuf) b).writeResourceLocation(id),
                (b, bounds) -> DimensionBounds.encode(bounds, (FriendlyByteBuf) b));
    }

    public static DimensionListS2CPacket decode(FriendlyByteBuf buf) {
        Map<ResourceLocation, DimensionBounds> map = buf.readMap(
                HashMap::new,
                b -> ((FriendlyByteBuf) b).readResourceLocation(),
                b -> DimensionBounds.decode((FriendlyByteBuf) b));
        return new DimensionListS2CPacket(map);
    }

    public static void send(ServerPlayer player, Map<ResourceLocation, DimensionBounds> dimensions) {
        ModPacketsCommon.SEND_TO_PLAYER.accept(player, new DimensionListS2CPacket(dimensions));
    }

    // クライアント側のみで処理するため受信コンテキストは不要
    public static void handle(DimensionListS2CPacket packet) {
        DimensionClientCache.setAll(packet.dimensions());
    }
}
