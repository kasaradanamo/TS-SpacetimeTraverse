package net.kasara.ts_spacetime_traverse.util;

import net.minecraft.network.FriendlyByteBuf;

public record DimensionBounds(int minY, int maxY, double minX, double maxX, double minZ, double maxZ) {

    // 26.2のSTREAM_CODECに相当する手動エンコード/デコード(1.20.1 SimpleChannel用)

    public static void encode(DimensionBounds bounds, FriendlyByteBuf buf) {
        buf.writeInt(bounds.minY());
        buf.writeInt(bounds.maxY());
        buf.writeDouble(bounds.minX());
        buf.writeDouble(bounds.maxX());
        buf.writeDouble(bounds.minZ());
        buf.writeDouble(bounds.maxZ());
    }

    public static DimensionBounds decode(FriendlyByteBuf buf) {
        return new DimensionBounds(
                buf.readInt(),
                buf.readInt(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
    }
}
