package net.kasara.ts_spacetime_traverse.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * ウェイポイント1件分のデータを保持するレコード
 *
 * @param uuid ウェイポイント固有UUID
 * @param name 表示名
 * @param dimension 対象ディメンション
 * @param blockPos テレポート先座標
 * @param yaw 到着時の向き
 */
public record WaypointData(UUID uuid, String name, ResourceKey<Level> dimension, BlockPos blockPos, int yaw) {

    // 26.2のSTREAM_CODECに相当する手動エンコード/デコード(1.20.1 SimpleChannel用)

    public static void encode(WaypointData data, FriendlyByteBuf buf) {
        buf.writeUUID(data.uuid());
        buf.writeUtf(data.name(), 64);
        buf.writeResourceKey(data.dimension());
        buf.writeBlockPos(data.blockPos());
        buf.writeInt(data.yaw());
    }

    public static WaypointData decode(FriendlyByteBuf buf) {
        return new WaypointData(
                buf.readUUID(),
                buf.readUtf(64),
                buf.readResourceKey(Registries.DIMENSION),
                buf.readBlockPos(),
                buf.readInt()
        );
    }
}
