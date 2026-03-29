package net.kasara.ts_spacetime_traverse.server;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.DimensionListS2CPacket;
import net.kasara.ts_spacetime_traverse.network.packet.s2c.WaypointInfoS2CPacket;
import net.kasara.ts_spacetime_traverse.util.DimensionBounds;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            WaypointInfoS2CPacket.send(player, WaypointServerManager.getAll(player), WaypointServerManager.getQuick(player));

            // クライアントにディメンションリストを送る
            sendDimensionList(server, player);
        });

        // プレイヤーがサーバーから切断したときの処理
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID uuid = handler.getPlayer().getUuid();
            // 所有しているポータルを全て破棄
            PortalHandler.discardOwnedPortals(server, uuid);
        });

        // サーバーが終了するとき
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
           // すべてのポータルを削除
           PortalHandler.discardAllPortals(server);
        });

        // プレイヤーエンティティがコピーされるとき（死亡・ディメンション移動など）
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            // Waypointデータを新しいプレイヤーに引き継ぐ
            WaypointServerManager.copyFrom(oldPlayer, newPlayer);
        });

        // ログ
        TSSpacetimeTraverse.LOGGER.info("Registering addon Server Events for "+ TokorotenSlimeAPI.getModId() +" (from " + TSSpacetimeTraverse.MOD_ID + ")");
    }

    /**
     * ディメンション名リストを作成してパケット送信
     */
    private static void sendDimensionList(MinecraftServer server, ServerPlayerEntity player) {
        Map<Identifier, DimensionBounds> map = new HashMap<>();

        for (RegistryKey<World> key : server.getWorldRegistryKeys()) {
            ServerWorld world = server.getWorld(key);
            if (world == null) continue;

            WorldBorder border = world.getWorldBorder();

            DimensionBounds info = new DimensionBounds(
                    world.getBottomY() + 1,
                    world.getTopYInclusive(),
                    border.getBoundWest(),
                    border.getBoundEast(),
                    border.getBoundNorth(),
                    border.getBoundSouth()
            );

            map.put(key.getValue(), info);
        }

        DimensionListS2CPacket.send(player, map);
    }
}
