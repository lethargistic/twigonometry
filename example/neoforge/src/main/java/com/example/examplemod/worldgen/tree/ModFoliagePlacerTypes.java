package com.example.examplemod.worldgen.tree;

import com.example.examplemod.Constants;
import com.example.examplemod.Shared;
import com.example.examplemod.worldgen.ModConfiguredFeatures;
import com.example.examplemod.worldgen.tree.foliage_placer.FancyExampleFoliagePlacer;
import com.example.examplemod.worldgen.tree.foliage_placer.SimpleExampleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.core.registries.BuiltInRegistries.FOLIAGE_PLACER_TYPE;

///
/// ##### **Vanilla setup:** Foliage Placer Types
/// These and {@link ModTrunkPlacerTypes} are put together into a feature in
/// {@link ModConfiguredFeatures}
/// which is also referenced for the sapling in {@link ModTreeGrowers}.
///
public class ModFoliagePlacerTypes {
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPES =
            DeferredRegister.create(FOLIAGE_PLACER_TYPE, Constants.MOD_ID);

    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<SimpleExampleFoliagePlacer>> SIMPLE_EXAMPLE_FOLIAGE_PLACER =
            FOLIAGE_PLACER_TYPES.register("simple_example_foliage_placer", () -> new FoliagePlacerType<>(SimpleExampleFoliagePlacer.CODEC));
    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<FancyExampleFoliagePlacer>> FANCY_EXAMPLE_FOLIAGE_PLACER =
            FOLIAGE_PLACER_TYPES.register("fancy_example_foliage_placer", () -> new FoliagePlacerType<>(FancyExampleFoliagePlacer.CODEC));

    /// We also assign shared values for these that can be seen in the common folder in {@link com.example.examplemod.ExampleMod}.
    /// This isn't necessary if you're not using Multiloader.
}