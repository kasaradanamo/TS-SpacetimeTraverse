package net.kasara.ts_spacetime_traverse.util;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DimensionBounds(int minY, int maxY, double minX, double maxX, double minZ, double maxZ) {

    public static final StreamCodec<RegistryFriendlyByteBuf, DimensionBounds> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    DimensionBounds::minY,
                    ByteBufCodecs.INT,
                    DimensionBounds::maxY,
                    ByteBufCodecs.DOUBLE,
                    DimensionBounds::minX,
                    ByteBufCodecs.DOUBLE,
                    DimensionBounds::maxX,
                    ByteBufCodecs.DOUBLE,
                    DimensionBounds::minZ,
                    ByteBufCodecs.DOUBLE,
                    DimensionBounds::maxZ,
                    DimensionBounds::new
            );
}