package net.kasara.ts_spacetime_traverse.server;

import net.kasara.ts_spacetime_traverse.network.packet.s2c.DimensionListS2CPacket;
import net.kasara.ts_spacetime_traverse.util.DimensionBounds;
import net.minecraft.resources.ResourceLocation;
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

    // ボーダーが変わったか
    private static boolean bordersChanged = false;

    /**
     * 各ディメンションの高さと境界を作成してパケット送信
     */
    public static void send(MinecraftServer server, ServerPlayer player) {
        Map<ResourceLocation, DimensionBounds> map = new HashMap<>();

        for (ResourceKey<Level> key : server.levelKeys()) {
            ServerLevel level = server.getLevel(key);
            if (level == null) continue;

            WorldBorder border = level.getWorldBorder();

            // 変化中は変化後の大きさ
            double half = border.getLerpTarget() / 2;
            int limit = border.getAbsoluteMaxSize();

            DimensionBounds info = new DimensionBounds(
                    level.getMinBuildHeight() + 1,
                    level.getMaxBuildHeight(),
                    Mth.clamp(border.getCenterX() - half, -limit, limit),
                    Mth.clamp(border.getCenterX() + half, -limit, limit),
                    Mth.clamp(border.getCenterZ() - half, -limit, limit),
                    Mth.clamp(border.getCenterZ() + half, -limit, limit)
            );

            map.put(key.location(), info);
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
     * ボーダーが変わったら印を付ける
     */
    public static void watchBorders(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            level.getWorldBorder().addListener(new BorderChangeListener() {
                @Override
                public void onBorderSizeSet(WorldBorder border, double size) {
                    bordersChanged = true;
                }

                @Override
                public void onBorderSizeLerping(WorldBorder border, double from, double to, long duration) {
                    bordersChanged = true;
                }

                @Override
                public void onBorderCenterSet(WorldBorder border, double x, double z) {
                    bordersChanged = true;
                }

                @Override
                public void onBorderSetWarningTime(WorldBorder border, int time) {}

                @Override
                public void onBorderSetWarningBlocks(WorldBorder border, int blocks) {}

                @Override
                public void onBorderSetDamagePerBlock(WorldBorder border, double damage) {}

                @Override
                public void onBorderSetDamageSafeZOne(WorldBorder border, double size) {}
            });
        }
    }

    /**
     * ボーダーが変わっていたら全員に送る
     */
    public static void sendIfBordersChanged(MinecraftServer server) {
        if (!bordersChanged) return;
        bordersChanged = false;
        sendAll(server);
    }

    private DimensionListSender() {}
}
