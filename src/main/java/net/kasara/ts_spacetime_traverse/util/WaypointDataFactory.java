package net.kasara.ts_spacetime_traverse.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WaypointDataFactory {

    private WaypointDataFactory() {}

    public static WaypointData fromInputs(@Nullable UUID uuid, String name, String dimensionText, int x, int y, int z, int yaw) {
        if (uuid == null) uuid = UUID.randomUUID();

        var dimension = (dimensionText == null || dimensionText.isBlank())
                ? World.OVERWORLD
                : RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimensionText));

        return new WaypointData(uuid, name, dimension, new BlockPos(x, y, z), yaw);
    }

    public static WaypointData fromInputs(@Nullable UUID uuid, String name, RegistryKey<World> dimension, int x, int y, int z, int yaw) {
        if (uuid == null) uuid = UUID.randomUUID();

        return new WaypointData(uuid, name, dimension, new BlockPos(x, y, z), yaw);
    }

    public static WaypointData fromInputs(@Nullable UUID uuid, String name, String dimensionText, BlockPos pos, int yaw) {
        if (uuid == null) uuid = UUID.randomUUID();

        var dimension = (dimensionText == null || dimensionText.isBlank())
                ? World.OVERWORLD
                : RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimensionText));

        return new WaypointData(uuid, name, dimension, pos, yaw);
    }

    public static WaypointData fromInputs(@Nullable UUID uuid, String name, RegistryKey<World> dimension, BlockPos pos, int yaw) {
        if (uuid == null) uuid = UUID.randomUUID();

        return new WaypointData(uuid, name, dimension, pos, yaw);
    }
}
