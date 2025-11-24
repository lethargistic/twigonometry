package com.example.examplemod;

import com.example.examplemod.worldgen.tree.foliage_placer.FancyExampleFoliagePlacer;
import com.example.examplemod.worldgen.tree.foliage_placer.SimpleExampleFoliagePlacer;
import com.example.examplemod.worldgen.tree.trunk_placer.FancyExampleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class Shared {
    public static FoliagePlacerType<SimpleExampleFoliagePlacer> SHARED_SIMPLE_EXAMPLE_FOLIAGE_PLACER;
    public static FoliagePlacerType<FancyExampleFoliagePlacer> SHARED_FANCY_EXAMPLE_FOLIAGE_PLACER;
    public static TrunkPlacerType<FancyExampleTrunkPlacer> SHARED_FANCY_EXAMPLE_TRUNK_PLACER;
}
