package com.example.examplemod.worldgen.tree;

import com.example.examplemod.worldgen.ModPlacedFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;

/// **Vanilla setup:** here we put our trees into the world.
/// This example uses BiomeModifications, for custom biomes e.g. with Terrablender or a datapack,
/// you add it to your biome builder (or .json entry) just as the other features.
public class ModTreeGeneration {
    /// I'm adding to random biomes for demonstration but note that your trees won't spawn if they can't spawn
    /// on the ground blocks of the biome, so e.g. stony shores would not work, you have to handle that separately
    public static void generateTrees() {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.SAVANNA, Biomes.SNOWY_PLAINS, Biomes.FOREST),
                GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.SIMPLE_EXAMPLE_TREE_PLACED_KEY)
        ;
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.MEADOW, Biomes.PLAINS, Biomes.GROVE),
                GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.FANCY_EXAMPLE_TREE_PLACED_KEY);
    }
}
