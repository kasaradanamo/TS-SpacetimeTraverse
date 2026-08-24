package net.kasara.ts_spacetime_traverse.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * WaypointData と NBT の相互変換ユーティリティ
 */
public class WaypointNbtUtil {

    // NBT内のキー名定義
    private static final String UUID_KEY = "uuid"; // ウェイポイントのUUID
    private static final String NAME = "name";     // ウェイポイント名
    private static final String DIM = "dim";       // 所属ワールド（Dimension）
    private static final String X = "x";           // 座標X
    private static final String Y = "y";           // 座標Y
    private static final String Z = "z";           // 座標Z
    private static final String YAW = "yaw";       // プレイヤー向き（角度）

    /**
     * WaypointData を NbtCompound に変換
     */
    public static CompoundTag toNbt(WaypointData data) {
        CompoundTag nbt = new CompoundTag();

        nbt.putString(UUID_KEY, data.uuid().toString());
        nbt.putString(NAME, data.name());
        nbt.putString(DIM, data.dimension().identifier().toString());

        // 座標を整数で保存
        nbt.putInt(X, data.blockPos().getX());
        nbt.putInt(Y, data.blockPos().getY());
        nbt.putInt(Z, data.blockPos().getZ());

        nbt.putInt(YAW, data.yaw());
        return nbt;
    }

    /**
     * NbtCompound から WaypointData に復元
     */
    public static WaypointData fromNbt(CompoundTag nbt) {
        UUID uuid = UUID.fromString(nbt.getString(UUID_KEY).orElseThrow());
        String name = nbt.getString(NAME).orElse("");
        ResourceKey<Level> dim =
                ResourceKey.create(Registries.DIMENSION,
                        Identifier.parse(nbt.getString(DIM).orElse("minecraft:overworld"))
                );

        BlockPos pos = new BlockPos(
                nbt.getInt(X).orElseThrow(),
                nbt.getInt(Y).orElseThrow(),
                nbt.getInt(Z).orElseThrow()
        );
        int yaw = nbt.getInt(YAW).orElse(0);

        return new WaypointData(uuid, name, dim, pos, yaw);
    }
}
