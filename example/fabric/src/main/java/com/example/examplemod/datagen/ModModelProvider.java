package com.example.examplemod.datagen;

import com.example.examplemod.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/// **Vanilla setup:** here we generate the .json models for the sapling,
/// so its texture is mapped onto it.
public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.createCrossBlock(ModBlocks.EXAMPLE_SAPLING, BlockModelGenerators.TintState.TINTED);
    }

    /// **Vanilla setup:** here we make a template to make a flat item using the texture from the textures/block folder.
    /// By default, it makes a cube 3d model of the block instead.
    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        flatBlockItem(ModBlocks.EXAMPLE_SAPLING, itemModelGenerator);
    }

    public static void flatBlockItem(Block block, ItemModelGenerators itemModelGenerator) {
        ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(block.asItem()),
                TextureMapping.layer0(TextureMapping.getBlockTexture(block)),
                itemModelGenerator.output
        );
    }
}
