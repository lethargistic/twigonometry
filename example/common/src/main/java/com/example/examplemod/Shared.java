package com.example.examplemod;

import com.example.examplemod.worldgen.tree.foliage_placer.ExampleFoliagePlacer;
import com.example.examplemod.worldgen.tree.trunk_placer.ExampleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class Shared {
    public static FoliagePlacerType<ExampleFoliagePlacer> SHARED_EXAMPLE_FOLIAGE_PLACER;
    public static TrunkPlacerType<ExampleTrunkPlacer> SHARED_EXAMPLE_TRUNK_PLACER;
}
