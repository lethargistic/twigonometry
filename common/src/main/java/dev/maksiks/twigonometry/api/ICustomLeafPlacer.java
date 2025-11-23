package dev.maksiks.twigonometry.api;

import net.minecraft.core.BlockPos;

@FunctionalInterface
public interface ICustomLeafPlacer {
    void place(BlockPos pos, int x, int z, int dist);
}