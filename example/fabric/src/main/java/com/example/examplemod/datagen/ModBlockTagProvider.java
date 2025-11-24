package com.example.examplemod.datagen;

import com.example.examplemod.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.CompletableFuture;

///  **Vanilla setup:** I think unlike the item tag, in vanilla this tag is useless,
/// but good riddance to have it for use in other mods anyway.
public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.SAPLINGS)
                .add(ModBlocks.SIMPLE_EXAMPLE_SAPLING)
                .add(ModBlocks.FANCY_EXAMPLE_SAPLING);
    }
}
