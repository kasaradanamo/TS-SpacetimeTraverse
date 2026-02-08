package net.kasara.ts_spacetime_traverse.util;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
public record WaypointData(UUID uuid, String name, RegistryKey<World> dimension, BlockPos blockPos, int yaw) {

    /**
     * PacketByteBuf にウェイポイント情報を書き込む
     */
    public void write(PacketByteBuf buf) {
        buf.writeUuid(uuid);
        buf.writeString(name);

        // RegistryKey<World>はIdentifierに変換して送信
        buf.writeIdentifier(dimension.getValue());

        buf.writeInt(blockPos.getX());
        buf.writeInt(blockPos.getY());
        buf.writeInt(blockPos.getZ());

        buf.writeInt(yaw);
    }

    /**
     * PacketByteBuf からウェイポイント情報を読み取り生成する
     */
    public static WaypointData read(PacketByteBuf buf) {
        UUID uuid = buf.readUuid();
        String name = buf.readString();

        // Identifier → RegistryKey<World>に復元
        Identifier dimId = buf.readIdentifier();
        RegistryKey<World> dimension = RegistryKey.of(RegistryKeys.WORLD, dimId);

        BlockPos pos = new BlockPos(
                buf.readInt(),
                buf.readInt(),
                buf.readInt()
        );
        int yaw = buf.readInt();

        return new WaypointData(uuid, name, dimension, pos, yaw);
    }
}
