package net.kasara.ts_spacetime_traverse.network.packet.c2s;

import net.kasara.ts_spacetime_traverse.network.ModPacketsCommon;
import net.kasara.ts_spacetime_traverse.server.PositionSwapServerHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * 位置入れ替え対象をサーバーに通知する
 */
public record PositionSwapC2SPacket(int targetId, ResourceKey<Level> dimension, boolean random) {

    public static void encode(PositionSwapC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.targetId());
        buf.writeResourceKey(packet.dimension());
        buf.writeBoolean(packet.random());
    }

    public static PositionSwapC2SPacket decode(FriendlyByteBuf buf) {
        return new PositionSwapC2SPacket(buf.readInt(), buf.readResourceKey(Registries.DIMENSION), buf.readBoolean());
    }

    public static void send(int targetId, ResourceKey<Level> dimension, boolean random) {
        ModPacketsCommon.SEND_TO_SERVER.accept(new PositionSwapC2SPacket(targetId, dimension, random));
    }

    public static void handle(PositionSwapC2SPacket packet, ServerPlayer sender) {
        if (sender != null) {
            PositionSwapServerHandler.positionSwap(packet.targetId(), packet.dimension(), sender, packet.random());
        }
    }
}
