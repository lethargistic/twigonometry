package com.example.examplemod.worldgen;

import com.example.examplemod.Constants;
import com.example.examplemod.worldgen.tree.foliage_placer.FancyExampleFoliagePlacer;
import com.example.examplemod.worldgen.tree.foliage_placer.SimpleExampleFoliagePlacer;
import com.example.examplemod.worldgen.tree.trunk_placer.FancyExampleTrunkPlacer;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

///
/// ##### **Vanilla setup:** Configured features
/// In Minecraft's worldgen system trees are features.
/// Features handle post-terrain block placement -
/// *flower patches, ores, fossils, basalt columns, corals, geodes, ice spikes, etc.*
///
/// This does not include structures (e.g. villages), surface rules and terrain itself though.
/// Features are meant mostly for small decorative placements, like trees.
///
/// {@link ModConfiguredFeatures} - configured features define **how** and **what** is placed when a tree or any other feature generates.
///
/// {@link ModPlacedFeatures} - placed features define **where** a feature is placed when it generates.
///
/// ##### Note:
/// If you don't know how to add trees to the game, I highly recommend Kaupenjoe's video tutorials. Here are the ones for Neo 1.21.1, it basically doesn't depend on mod loader tho:
/// - [NeoForge Modding Tutorial - Minecraft 1.21.1: Custom Tree | #35](https://www.youtube.com/watch?v=UBYntJHQmgA&list=PLKGarocXCE1G6CQOoiYdMVx-E1d9F_itF&index=35)
/// - [NeoForge Modding Tutorial - Minecraft 1.21.1: Tree Generation | #36](https://www.youtube.com/watch?v=5_4mEDHqUR0&list=PLKGarocXCE1G6CQOoiYdMVx-E1d9F_itF&index=36)
///
public class ModConfiguredFeatures {
    public static ResourceKey<ConfiguredFeature<?, ?>> SIMPLE_EXAMPLE_TREE_KEY = registerKey("simple_example_tree");
    public static ResourceKey<ConfiguredFeature<?, ?>> FANCY_EXAMPLE_TREE_KEY = registerKey("fancy_example_tree");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        /// nice to have thingie, can be used with e.g.
        /// PlacementUtils.inlinePlaced(configuredFeatures.getOrThrow(ModConfiguredFeatures.FEATURE))
        /// to not make a separate placed feature every time
        // HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        /// You may also choose to use vanilla's placers from
        /// - {@link net.minecraft.world.level.levelgen.feature.foliageplacers)
        /// - {@link net.minecraft.world.level.levelgen.feature.trunkplacers)
        register(
                context,
                SIMPLE_EXAMPLE_TREE_KEY,
                Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.STRIPPED_DARK_OAK_LOG),
                        new StraightTrunkPlacer(8, 1, 0),
                        BlockStateProvider.simple(Blocks.CHERRY_LEAVES),
                        new SimpleExampleFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 0),
                        new TwoLayersFeatureSize(1, 0, 1)).build()
        );

        register(
                context,
                FANCY_EXAMPLE_TREE_KEY,
                Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.STRIPPED_SPRUCE_LOG),
                        new FancyExampleTrunkPlacer(6, 0, 0),
                        new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                                .add(Blocks.AZALEA_LEAVES.defaultBlockState(), 4)
                                .add(Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState(), 1)
                                .build()),
                        new FancyExampleFoliagePlacer(ConstantInt.of(5), ConstantInt.of(0), 0),
                        new TwoLayersFeatureSize(1, 0, 1)).build()
        );
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
