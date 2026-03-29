package net.kasara.ts_spacetime_traverse.util;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Uuids;
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

    public static final PacketCodec<RegistryByteBuf, WaypointData> PACKET_CODEC =
            PacketCodec.tuple(
                    Uuids.PACKET_CODEC,
                    WaypointData::uuid,
                    PacketCodecs.string(64),
                    WaypointData::name,
                    RegistryKey.createPacketCodec(RegistryKeys.WORLD),
                    WaypointData::dimension,
                    BlockPos.PACKET_CODEC,
                    WaypointData::blockPos,
                    PacketCodecs.INTEGER,
                    WaypointData::yaw,
                    WaypointData::new
            );
}
