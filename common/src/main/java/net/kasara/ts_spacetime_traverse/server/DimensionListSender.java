package net.kasara.ts_spacetime_traverse.server;

import net.kasara.ts_spacetime_traverse.network.packet.s2c.DimensionListS2CPacket;
import net.kasara.ts_spacetime_traverse.util.DimensionBounds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;

import java.util.HashMap;
import java.util.Map;

/**
 * ディメンションの一覧をクライアントへ送るクラス
 */
public final class DimensionListSender {

    /**
     * 各ディメンションの高さと境界を作成してパケット送信
     */
    public static void send(MinecraftServer server, ServerPlayer player) {
        Map<Identifier, DimensionBounds> map = new HashMap<>();

        for (ResourceKey<Level> key : server.levelKeys()) {
            ServerLevel level = server.getLevel(key);
            if (level == null) continue;

            WorldBorder border = level.getWorldBorder();

            // 変化中は変化後の大きさ
            double half = border.getLerpTarget() / 2;
            int limit = border.getAbsoluteMaxSize();

            DimensionBounds info = new DimensionBounds(
                    level.getMinY() + 1,
                    level.getMaxY() + 1,
                    Mth.clamp(border.getCenterX() - half, -limit, limit),
                    Mth.clamp(border.getCenterX() + half, -limit, limit),
                    Mth.clamp(border.getCenterZ() - half, -limit, limit),
                    Mth.clamp(border.getCenterZ() + half, -limit, limit)
            );

            map.put(key.identifier(), info);
        }

        DimensionListS2CPacket.send(player, map);
    }

    /**
     * 全員に送る
     */
    public static void sendAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            send(server, player);
        }
    }

    /**
     * ボーダーが変わったら送り直す
     */
    public static void watchBorders(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            level.getWorldBorder().addListener(new BorderChangeListener() {
                @Override
                public void onSetSize(WorldBorder border, double size) {
                    sendAll(server);
                }

                @Override
                public void onLerpSize(WorldBorder border, double from, double to, long duration, long gameTime) {
                    sendAll(server);
                }

                @Override
                public void onSetCenter(WorldBorder border, double x, double z) {
                    sendAll(server);
                }

                @Override
                public void onSetWarningTime(WorldBorder border, int time) {}

                @Override
                public void onSetWarningBlocks(WorldBorder border, int blocks) {}

                @Override
                public void onSetDamagePerBlock(WorldBorder border, double damage) {}

                @Override
                public void onSetSafeZone(WorldBorder border, double size) {}
            });
        }
    }

    private DimensionListSender() {}
}
