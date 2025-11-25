package com.example.examplemod.worldgen;

import com.example.examplemod.Constants;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/// **Vanilla setup:** here we put our trees into the world.
/// This example uses BiomeModifications, for custom biomes e.g. with Terrablender or a datapack,
/// you add it to your biome builder (or .json entry) just as the other features.
public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_SIMPLE_EXAMPLE_TREE = registerKey("add_simple_example_tree");
    public static final ResourceKey<BiomeModifier> ADD_FANCY_EXAMPLE_TREE = registerKey("add_fancy_example_tree");

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        /// I'm adding to random biomes for demonstration but note that your trees won't spawn if they can't spawn
        /// on the ground blocks of the biome, so e.g. stony shores would not work, you have to handle that separately
        context.register(ADD_SIMPLE_EXAMPLE_TREE, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.SAVANNA), biomes.getOrThrow(Biomes.SNOWY_PLAINS), biomes.getOrThrow(Biomes.FOREST)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.SIMPLE_EXAMPLE_TREE_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(ADD_FANCY_EXAMPLE_TREE, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.MEADOW), biomes.getOrThrow(Biomes.PLAINS), biomes.getOrThrow(Biomes.GROVE)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.FANCY_EXAMPLE_TREE_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));

    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
    }
}
