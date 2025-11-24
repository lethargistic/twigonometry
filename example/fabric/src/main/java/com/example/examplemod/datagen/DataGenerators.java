package com.example.examplemod.datagen;

import com.example.examplemod.worldgen.ModConfiguredFeatures;
import com.example.examplemod.worldgen.ModPlacedFeatures;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;


/// **Vanilla setup:** here we generate the models, loot tables and tags for our sapling.
/// Make sure to run the Data Generation (:fabric) run config before joining the game.
public class DataGenerators implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(ModModelProvider::new);
        pack.addProvider(ModBlockLootTableProvider::new);
        FabricTagProvider.BlockTagProvider blockTagProvider = pack.addProvider(ModBlockTagProvider::new);
        pack.addProvider((output, registriesFuture) ->
                new ModItemTagProvider(output, registriesFuture, blockTagProvider));
        pack.addProvider(ModRegistryDataGenerator::new);
    }

    /// **Vanilla setup:** here we attach our configured features and
    /// placed features to the datagen
    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap);
        registryBuilder.add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap);
    }

}
