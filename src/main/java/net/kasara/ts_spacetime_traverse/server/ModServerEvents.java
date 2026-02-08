package net.kasara.ts_spacetime_traverse.server;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.DimensionListS2CPacket;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.WaypointInfoS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

/**
 * サーバー側の各種イベントを登録するクラス
 */
public class ModServerEvents {

    /**
     * サーバー関連イベントの登録処理
     */
    public static void registerEvents() {

        // プレイヤーがサーバーに参加したときの処理
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            // クライアントにnbt情報を伝える
            WaypointInfoS2CPacket.send(player, ServerWaypointManager.getAll(player), ServerWaypointManager.getQuick(player));

            // クライアントにディメンションリストを送る
            sendDimensionList(server, player);
        });

        // プレイヤーがサーバーから切断したときの処理
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID uuid = handler.getPlayer().getUuid();
            // 所有しているポータルを全て破棄
            ServerPortalHandler.discardOwnedPortals(server, uuid);
        });

        // プレイヤーエンティティがコピーされるとき（死亡・ディメンション移動など）
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            // Waypointデータを新しいプレイヤーに引き継ぐ
            ServerWaypointManager.copyFrom(oldPlayer, newPlayer);
        });

        // ログ
        TSSpacetimeTraverse.LOGGER.info("Registering addon Server Events for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }

    /**
     * ディメンション名リストを作成してパケット送信
     */
    private static void sendDimensionList(MinecraftServer server, ServerPlayerEntity player) {
        Set<Identifier> dimensions = server.getRegistryManager()
                .getOrThrow(RegistryKeys.WORLD)
                .streamKeys()
                .map(RegistryKey::getValue)
                .collect(Collectors.toSet());

        DimensionListS2CPacket.send(player, dimensions);
    }
}
