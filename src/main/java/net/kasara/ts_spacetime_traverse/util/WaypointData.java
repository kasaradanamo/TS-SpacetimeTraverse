package net.kasara.ts_spacetime_traverse.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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

    public static final StreamCodec<RegistryFriendlyByteBuf, WaypointData> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    WaypointData::uuid,
                    ByteBufCodecs.stringUtf8(64),
                    WaypointData::name,
                    ResourceKey.streamCodec(Registries.DIMENSION),
                    WaypointData::dimension,
                    BlockPos.STREAM_CODEC,
                    WaypointData::blockPos,
                    ByteBufCodecs.INT,
                    WaypointData::yaw,
                    WaypointData::new
            );
}
