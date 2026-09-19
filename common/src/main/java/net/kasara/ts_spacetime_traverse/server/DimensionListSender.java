package net.kasara.ts_spacetime_traverse.server;

import net.kasara.ts_spacetime_traverse.network.packet.s2c.DimensionListS2CPacket;
import net.kasara.ts_spacetime_traverse.util.DimensionBounds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;

import java.util.HashMap;
import java.util.Map;

/**
 * ディメンションの一覧をクライアントへ送るクラス
 */
public final class DimensionListSender {

    /**
     * ディメンション名リストを作成してパケット送信
     */
    public static void send(MinecraftServer server, ServerPlayer player) {
        Map<Identifier, DimensionBounds> map = new HashMap<>();

        for (ResourceKey<Level> key : server.levelKeys()) {
            ServerLevel level = server.getLevel(key);
            if (level == null) continue;

            WorldBorder border = level.getWorldBorder();

            DimensionBounds info = new DimensionBounds(
                    level.getMinY() + 1,
                    level.getMaxY(),
                    border.getMinX(),
                    border.getMaxX(),
                    border.getMinZ(),
                    border.getMaxZ()
            );

            map.put(key.identifier(), info);
        }

        DimensionListS2CPacket.send(player, map);
    }

    private DimensionListSender() {}
}
