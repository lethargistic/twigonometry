package com.example.examplemod.worldgen.tree;

import com.example.examplemod.Constants;
import com.example.examplemod.Shared;
import com.example.examplemod.worldgen.ModConfiguredFeatures;
import com.example.examplemod.worldgen.tree.foliage_placer.FancyExampleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.core.registries.BuiltInRegistries.FOLIAGE_PLACER_TYPE;

///
/// ##### Foliage Placer Types
/// These and {@link ModTrunkPlacerTypes} are put together into a feature in
/// {@link ModConfiguredFeatures}
/// which is also referenced for the sapling in {@link ModTreeGrowers}.
///
public class ModFoliagePlacerTypes {
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPES =
            DeferredRegister.create(FOLIAGE_PLACER_TYPE, Constants.MOD_ID);

    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<FancyExampleFoliagePlacer>> EXAMPLE_FOLIAGE_PLACER =
            FOLIAGE_PLACER_TYPES.register("example_foliage_placer", () -> new FoliagePlacerType<>(FancyExampleFoliagePlacer.CODEC));

    /// Here we assign a shared variable for the common folder so we don't have to duplicate code
    /// for each modloader. This isn't necessary if you're not using Multiloader.
    static {
        Shared.SHARED_FANCY_EXAMPLE_FOLIAGE_PLACER = EXAMPLE_FOLIAGE_PLACER.get();
    }
}