package com.example.examplemod.worldgen.tree;

import com.example.examplemod.Constants;
import com.example.examplemod.Shared;
import com.example.examplemod.worldgen.ModConfiguredFeatures;
import com.example.examplemod.worldgen.tree.trunk_placer.ExampleTrunkPlacer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

///
/// ##### **Vanilla setup:** Trunk Placer Types
/// These and {@link ModFoliagePlacerTypes} are put together into a feature in
/// {@link ModConfiguredFeatures}
/// which is also referenced for a sapling in {@link ModTreeGrowers}.
///
public class ModTrunkPlacerTypes {
    public static final TrunkPlacerType<ExampleTrunkPlacer> EXAMPLE_TRUNK_PLACER =
            register("example_trunk_placer", new TrunkPlacerType<>(ExampleTrunkPlacer.CODEC));

    private static <P extends TrunkPlacerType<?>> P register(String name, P placerType) {
        return Registry.register(BuiltInRegistries.TRUNK_PLACER_TYPE,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                placerType);
    }

    public static void initialize() {
        /// Vanilla setup: Here we assign a shared variable for the common folder so we don't have to duplicate code
        /// for each modloader. This isn't necessary if you're not using Multiloader.
        Shared.SHARED_EXAMPLE_TRUNK_PLACER = EXAMPLE_TRUNK_PLACER;
    }
}
