package com.example.examplemod.worldgen.tree;

import com.example.examplemod.Constants;
import com.example.examplemod.Shared;
import com.example.examplemod.worldgen.ModConfiguredFeatures;
import com.example.examplemod.worldgen.tree.foliage_placer.FancyExampleFoliagePlacer;
import com.example.examplemod.worldgen.tree.foliage_placer.SimpleExampleFoliagePlacer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

///
/// ##### **Vanilla setup:** Foliage Placer Types
/// These and {@link ModTrunkPlacerTypes} are put together into a feature in
/// {@link ModConfiguredFeatures}
/// which is also referenced for the sapling in {@link ModTreeGrowers}.
///
public class ModFoliagePlacerTypes {
    public static final FoliagePlacerType<SimpleExampleFoliagePlacer> SIMPLE_EXAMPLE_FOLIAGE_PLACER =
            register("simple_example_foliage_placer", new FoliagePlacerType<>(SimpleExampleFoliagePlacer.CODEC));
    public static final FoliagePlacerType<FancyExampleFoliagePlacer> FANCY_EXAMPLE_FOLIAGE_PLACER =
            register("fancy_example_foliage_placer", new FoliagePlacerType<>(FancyExampleFoliagePlacer.CODEC));

    private static <P extends FoliagePlacerType<?>> P register(String name, P placerType) {
        return Registry.register(BuiltInRegistries.FOLIAGE_PLACER_TYPE,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                placerType);
    }

    public static void initialize() {
        /// Vanilla setup: Here we assign a shared variable for the common folder so we don't have to duplicate code
        /// for each modloader. This isn't necessary if you're not using Multiloader.
        Shared.SIMPLE_FOLIAGE_SUPPLIER = () -> SIMPLE_EXAMPLE_FOLIAGE_PLACER;
        Shared.FANCY_FOLIAGE_SUPPLIER = () -> FANCY_EXAMPLE_FOLIAGE_PLACER;
    }
}