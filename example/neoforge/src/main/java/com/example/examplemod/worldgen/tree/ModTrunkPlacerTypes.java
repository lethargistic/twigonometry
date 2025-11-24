package com.example.examplemod.worldgen.tree;

import com.example.examplemod.Constants;
import com.example.examplemod.Shared;
import com.example.examplemod.worldgen.ModConfiguredFeatures;
import com.example.examplemod.worldgen.tree.trunk_placer.FancyExampleTrunkPlacer;
import com.example.examplemod.worldgen.tree.trunk_placer.SimpleExampleTrunkPlacer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

///
/// ##### **Vanilla setup:** Trunk Placer Types
/// These and {@link ModFoliagePlacerTypes} are put together into a feature in
/// {@link ModConfiguredFeatures}
/// which is also referenced for a sapling in {@link ModTreeGrowers}.
///
public class ModTrunkPlacerTypes {
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER_TYPES =
            DeferredRegister.create(BuiltInRegistries.TRUNK_PLACER_TYPE, Constants.MOD_ID);

    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<SimpleExampleTrunkPlacer>> SIMPLE_EXAMPLE_TRUNK_PLACER =
            TRUNK_PLACER_TYPES.register("simple_example_trunk_placer", () -> new TrunkPlacerType<>(SimpleExampleTrunkPlacer.CODEC));
    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<FancyExampleTrunkPlacer>> FANCY_EXAMPLE_TRUNK_PLACER =
            TRUNK_PLACER_TYPES.register("fancy_example_trunk_placer", () -> new TrunkPlacerType<>(FancyExampleTrunkPlacer.CODEC));

    /// Here we assign a shared variable for the common folder so we don't have to duplicate code
    /// for each modloader. This isn't necessary if you're not using Multiloader.
    static {
        Shared.SHARED_FANCY_EXAMPLE_TRUNK_PLACER = FANCY_EXAMPLE_TRUNK_PLACER.get();
        Shared.SHARED_SIMPLE_EXAMPLE_TRUNK_PLACER = SIMPLE_EXAMPLE_TRUNK_PLACER.get();
    }
}
