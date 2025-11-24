package com.example.examplemod;

import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.function.Supplier;

public class Shared {
    public static Supplier<TrunkPlacerType<?>> FANCY_TRUNK_SUPPLIER;
    public static Supplier<FoliagePlacerType<?>> SIMPLE_FOLIAGE_SUPPLIER;
    public static Supplier<FoliagePlacerType<?>> FANCY_FOLIAGE_SUPPLIER;
}
