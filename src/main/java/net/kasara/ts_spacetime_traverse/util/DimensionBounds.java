package net.kasara.ts_spacetime_traverse.util;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record DimensionBounds(int minY, int maxY, double minX, double maxX, double minZ, double maxZ) {

    public static final PacketCodec<RegistryByteBuf, DimensionBounds> PACKET_CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER,
                    DimensionBounds::minY,
                    PacketCodecs.INTEGER,
                    DimensionBounds::maxY,
                    PacketCodecs.DOUBLE,
                    DimensionBounds::minX,
                    PacketCodecs.DOUBLE,
                    DimensionBounds::maxX,
                    PacketCodecs.DOUBLE,
                    DimensionBounds::minZ,
                    PacketCodecs.DOUBLE,
                    DimensionBounds::maxZ,
                    DimensionBounds::new
            );
}
