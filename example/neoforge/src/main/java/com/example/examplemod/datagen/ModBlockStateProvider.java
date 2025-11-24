package com.example.examplemod.datagen;

import com.example.examplemod.Constants;
import com.example.examplemod.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

/// **Vanilla setup:** here we generate the .json models for the sapling,
/// so its texture is mapped onto it.
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Constants.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        crossBlock(ModBlocks.SIMPLE_EXAMPLE_SAPLING);
        crossBlock(ModBlocks.FANCY_EXAMPLE_SAPLING);
    }

    /// importantly there's renderType("cutout") to allow for transparent pixels
    /// otherwise the transparent bits of the sapling would look black
    private void crossBlock(DeferredBlock<Block> blockRegistryObject) {
        simpleBlock(blockRegistryObject.get(),
                models().cross(BuiltInRegistries.BLOCK.getKey(blockRegistryObject.get()).getPath(),
                        blockTexture(blockRegistryObject.get())).renderType("cutout"));
    }

}
