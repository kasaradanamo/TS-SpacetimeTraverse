package net.kasara.ts_spacetime_traverse.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class WaypointDataUtil {

    public static WaypointData fromInputs(@Nullable UUID uuid, String name, String dimensionText, int x, int y, int z, int yaw) {
        if (uuid == null) uuid = UUID.randomUUID();

        var dimension = (dimensionText == null || dimensionText.isBlank())
                ? Level.OVERWORLD
                : ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimensionText));

        return new WaypointData(uuid, name, dimension, new BlockPos(x, y, z), yaw);
    }

    public static WaypointData fromInputs(@Nullable UUID uuid, String name, ResourceKey<Level> dimension, int x, int y, int z, int yaw) {
        if (uuid == null) uuid = UUID.randomUUID();

        return new WaypointData(uuid, name, dimension, new BlockPos(x, y, z), yaw);
    }

    public static WaypointData fromInputs(@Nullable UUID uuid, String name, String dimensionText, BlockPos pos, int yaw) {
        if (uuid == null) uuid = UUID.randomUUID();

        var dimension = (dimensionText == null || dimensionText.isBlank())
                ? Level.OVERWORLD
                : ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimensionText));

        return new WaypointData(uuid, name, dimension, pos, yaw);
    }

    public static WaypointData fromInputs(@Nullable UUID uuid, String name, ResourceKey<Level> dimension, BlockPos pos, int yaw) {
        if (uuid == null) uuid = UUID.randomUUID();

        return new WaypointData(uuid, name, dimension, pos, yaw);
    }

    private WaypointDataUtil() {}
}
