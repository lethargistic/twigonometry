package com.example.examplemod.worldgen;

import com.example.examplemod.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

///
/// ##### **Vanilla setup:** Placed Features
/// This example provides tree generation with a biome modifier,
/// if you're adding a custom biome, e.g. with Terrablender or a datapack,
/// you'd have to add it in its biome builder (or .json entry) just as the other features.
///
public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> SIMPLE_EXAMPLE_TREE_PLACED_KEY = registerKey("simple_example_tree_placed");
    public static final ResourceKey<PlacedFeature> FANCY_EXAMPLE_TREE_PLACED_KEY = registerKey("fancy_example_tree_placed");

    ///
    /// Here we register the datagen placed feature for the tree using PlacementUtils.countExtra
    /// whose args are:
    /// - baseValue: 1 - guaranteed minimum number to place per chunk (may fail)
    /// - chance: 0.1f - chance for an extra tree
    /// - addedAmount: 1 - how many extra ones to place if the chance procs
    ///
    /// You could also set baseValue to 0 if you want, for example, 1 tree per 10 chunks.
    ///
    /// These may fail on placement for certain trees and that's ok, make sure to adjust chances accordingly.
    /// If you have gigantic trees even if the game tries to place 1 per chunk, most of the time it will fail
    /// simply because there's not enough space.
    ///
    /// <p color="fa6b64"><b>IMPORTANT</b>: 1/chance of countExtra must always be an integer or your game will crash with `Chance data cannot be represented as list weight`,
    /// because Mojank.
    ///
    /// If you just want rarer placements you can use RarityFilter.onAverageOnceEvery(x chunks).</p>
    ///
    /// <p color="fa6b64"><b>IMPORTANT</b>: the sapling block determines valid placement blocks for worldgen.
    /// Without it, trees may spawn in invalid locations (e.g., stacked on each other).</p>
    ///
    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, SIMPLE_EXAMPLE_TREE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.SIMPLE_EXAMPLE_TREE_KEY),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(1, 0.1f, 1),
                        Blocks.SPRUCE_SAPLING));

        register(context, FANCY_EXAMPLE_TREE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.FANCY_EXAMPLE_TREE_KEY),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(1, 0.1f, 1),
                        Blocks.SPRUCE_SAPLING));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
