package com.example.examplemod.datagen;

import com.example.examplemod.Constants;
import com.example.examplemod.block.ModBlocks;
import com.example.examplemod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

/// **Vanilla setup:** here we generate the item models for the saplings.
public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        flatBlockItem(ModBlocks.SIMPLE_EXAMPLE_SAPLING);
        flatBlockItem(ModBlocks.FANCY_EXAMPLE_SAPLING);
    }

    private void flatBlockItem(DeferredBlock<Block> item) {
        withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block/" + item.getId().getPath()));
    }
}
