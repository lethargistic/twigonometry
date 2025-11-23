package com.example.examplemod.worldgen.tree;

import com.example.examplemod.Constants;
import com.example.examplemod.Shared;
import com.example.examplemod.worldgen.ModConfiguredFeatures;
import com.example.examplemod.worldgen.tree.trunk_placer.ExampleTrunkPlacer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

///
/// ##### Trunk Placer Types
/// These and {@link ModFoliagePlacerTypes} are put together into a feature in
/// {@link ModConfiguredFeatures}
/// which is also referenced for a sapling in {@link ModTreeGrowers}.
///
public class ModTrunkPlacerTypes {
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER_TYPES =
            DeferredRegister.create(BuiltInRegistries.TRUNK_PLACER_TYPE, Constants.MOD_ID);

    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<ExampleTrunkPlacer>> EXAMPLE_TRUNK_PLACER =
            TRUNK_PLACER_TYPES.register("example_trunk_placer", () -> new TrunkPlacerType<>(ExampleTrunkPlacer.CODEC));

    /// Here we assign a shared variable for the common folder so we don't have to duplicate code
    /// for each modloader. This isn't necessary if you're not using Multiloader.
    static {
        Shared.SHARED_EXAMPLE_TRUNK_PLACER = EXAMPLE_TRUNK_PLACER.get();
    }
}
