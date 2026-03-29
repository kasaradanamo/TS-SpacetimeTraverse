package net.kasara.ts_spacetime_traverse.server;

import net.kasara.tokorotenslime.api.TokorotenSlimeAPI;
import net.kasara.ts_spacetime_traverse.TSSpacetimeTraverse;
import net.kasara.ts_spacetime_traverse.util.WaypointData;
import net.kasara.ts_spacetime_traverse.util.WaypointNbtUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * サーバー側でプレイヤーのウェイポイントを管理するクラス
 */
public final class WaypointServerManager {

    private static final String WAYPOINTS = "waypoints";
    private static final String QUICK_UUID = "quick_uuid";
    private static final int MAX_WAYPOINTS = 10;

    /**
     * 全ウェイポイントを取得
     *
     * @param player 対象のプレイヤー
     */
    public static List<WaypointData> getAll(ServerPlayer player) {
        return getRoot(player).getWaypointList();
    }

    /**
     * 指定UUIDのウェイポイントを取得
     *
     * @param player 対象のプレイヤー
     * @param uuid waypointのUUID
     */
    public static @Nullable WaypointData get(ServerPlayer player, UUID uuid) {
        return getRoot(player).getWaypointList().stream()
                .filter(wp -> wp.uuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    /**
     * クイックウェイポイントのUUIDを取得
     *
     * @param player 対象のプレイヤー
     */
    public static @Nullable UUID getQuick(ServerPlayer player) {
        return getRoot(player).getQuick();
    }

    /**
     * ウェイポイントを追加・更新
     *
     * @param player 対象のプレイヤー
     * @param data waypointのdata
     */
    public static void upsert(ServerPlayer player, WaypointData data) {
        WaypointRoot root = getRoot(player);
        List<WaypointData> list = root.getWaypointList();

        boolean replaced = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).uuid().equals(data.uuid())) {
                list.set(i, data);
                replaced = true;
                break;
            }
        }

        if (!replaced && list.size() < MAX_WAYPOINTS) {
            if (list.isEmpty()) {
                root.setQuick(data.uuid());
            }
            list.add(data);
        }

        root.setWaypointList(list);
        writeRoot(player, root);
    }

    /**
     * ウェイポイントを削除
     *
     * @param player 対象のプレイヤー
     * @param uuid 対象のwaypointのUUID
     */
    public static void remove(ServerPlayer player, UUID uuid) {
        WaypointRoot root = getRoot(player);
        List<WaypointData> list = root.getWaypointList();
        list.removeIf(wp -> uuid.equals(wp.uuid()));
        if (uuid.equals(root.getQuick())) {
            root.setQuick(null);
        }
        root.setWaypointList(list);
        writeRoot(player, root);
    }

    /**
     * クイックウェイポイントを設定
     *
     * @param player 対象のプレイヤー
     * @param uuid 対象のwaypointのUUID(nullをセットする場合もある)
     */
    public static void setQuick(ServerPlayer player, @Nullable UUID uuid) {
        WaypointRoot root = getRoot(player);
        root.setQuick(uuid);
        writeRoot(player, root);
    }

    /**
     * リスポーン・ディメンション移動の際コピーする
     *
     * @param oldPlayer コピー元のプレイヤー
     * @param newPlayer コピー先のプレイヤー
     */
    public static void copyFrom(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        writeRoot(newPlayer, getRoot(oldPlayer).copy());
    }

    /**
     * 追加するか消すか。追加する前に、ワールドあるか確認
     *
     * @param player 対象のプレイヤー
     * @param data waypointのdata
     * @param delete 削除かどうか(削除ならtrue/追加ならfalse)
     */
    public static void applyWaypointChange(ServerPlayer player, WaypointData data, boolean delete) {
        if (!delete) {
            ServerLevel world = player.level().getServer().getLevel(data.dimension());
            if (world == null) return;
            upsert(player, data);
        } else {
            remove(player, data.uuid());
        }
    }

    /**
     * AddonCustomDataからRootを取得
     */
    private static WaypointRoot getRoot(Player player) {
        CompoundTag nbt = TokorotenSlimeAPI.getAddonData(player, TSSpacetimeTraverse.MOD_ID);
        return new WaypointRoot(nbt);
    }

    /**
     * AddonCustomDataに書き込み
     */
    private static void writeRoot(ServerPlayer player, WaypointRoot root) {
        TokorotenSlimeAPI.writeAddonData(player, TSSpacetimeTraverse.MOD_ID, root.toCompound());
    }

    /**
     * NBTとウェイポイントリストを同期して管理
     */
    private record WaypointRoot(CompoundTag root) {

        /**
         * NBTから全ウェイポイントを読み込みListに変換
         */
        List<WaypointData> getWaypointList() {
            return fromNbtList(root.getListOrEmpty(WAYPOINTS));
        }

        /**
         * ListからNBTに変換してセット
         */
        void setWaypointList(List<WaypointData> list) {
            root.put(WAYPOINTS, toNbtList(list));
        }

        /**
         * クイックウェイポイントUUIDを取得
         */
        @Nullable UUID getQuick() {
            return root.getString(QUICK_UUID).map(UUID::fromString).orElse(null);
        }

        /**
         * クイックウェイポイントUUIDを設定または解除
         */
        void setQuick(@Nullable UUID uuid) {
            if (uuid == null) root.remove(QUICK_UUID);
            else root.putString(QUICK_UUID, uuid.toString());
        }

        /**
         * NBTを外部に返す
         */
        CompoundTag toCompound() {
            return root;
        }

        /**
         * NBTをコピーして新しいWaypointRootを作成
         */
        WaypointRoot copy() {
            CompoundTag newRoot = root.copy();
            return new WaypointRoot(newRoot);
        }

        /**
         * NBTリストからWaypointDataのListを作成
         */
        private List<WaypointData> fromNbtList(ListTag nbtList) {
            List<WaypointData> list = new ArrayList<>();
            for (int i = 0; i< nbtList.size(); i++) {
                list.add(WaypointNbtUtil.fromNbt(nbtList.getCompound(i).orElse(new CompoundTag())));
            }
            return list;
        }

        /**
         * WaypointDataのListをNBTリストに変換
         */
        private ListTag toNbtList(List<WaypointData> list) {
            ListTag nbtList = new ListTag();
            for (WaypointData wp : list) {
                nbtList.add(WaypointNbtUtil.toNbt(wp));
            }
            return nbtList;
        }
    }

    private WaypointServerManager() {}
}
