package net.kasara.ts_spacetime_traverse.fabric.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kasara.tokorotenslime.TokorotenSlimeCommon;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverseCommon;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.DimensionListS2CPacket;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.WaypointInfoS2CPacket;
import net.kasara.ts_spacetime_traverse.server.PortalHandler;
import net.kasara.ts_spacetime_traverse.server.PortalManager;
import net.kasara.ts_spacetime_traverse.server.WaypointServerManager;
import net.kasara.ts_spacetime_traverse.util.DimensionBounds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;

import java.util.HashMap;
import java.util.Map;

/**
 * サーバー側の各種イベントを登録するクラス
 */
public class ModServerEvents {

    /**
     * サーバー関連イベントの登録処理
     */
    public static void register() {
        // プレイヤーがサーバーに参加したときの処理
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;

            // クライアントにnbt情報を伝える
            WaypointInfoS2CPacket.send(player, WaypointServerManager.getAll(player), WaypointServerManager.getQuick(player));

            // クライアントにディメンションリストを送る
            sendDimensionList(player.serverLevel().getServer(), player);
        });

        // プレイヤーがサーバーから切断したときの処理
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;

            // 所有しているポータルを全て破棄
            PortalHandler.discardOwnedPortals(player.serverLevel().getServer(), player.getUUID());
        });

        // サーバーtickごとにポータルのあるディメンションを稼働させ続ける
        ServerTickEvents.END_SERVER_TICK.register(server -> PortalManager.keepPortalDimensionsActive());

        // サーバーが終了するとき
        ServerLifecycleEvents.SERVER_STOPPING.register(PortalHandler::discardAllPortals);

        TSSpacetimeTraverseCommon.LOGGER.info("Registering addon Server Events for " + TokorotenSlimeCommon.MOD_ID + " (from " + TSSpacetimeTraverseCommon.MOD_ID + ")");
    }

    /**
     * ディメンション名リストを作成してパケット送信
     */
    private static void sendDimensionList(MinecraftServer server, ServerPlayer player) {
        Map<ResourceLocation, DimensionBounds> map = new HashMap<>();

        for (ResourceKey<Level> key : server.levelKeys()) {
            var level = server.getLevel(key);
            if (level == null) continue;

            WorldBorder border = level.getWorldBorder();

            DimensionBounds info = new DimensionBounds(
                    level.getMinBuildHeight() + 1,
                    level.getMaxBuildHeight(),
                    border.getMinX(),
                    border.getMaxX(),
                    border.getMinZ(),
                    border.getMaxZ()
            );

            map.put(key.location(), info);
        }

        DimensionListS2CPacket.send(player, map);
    }
}
