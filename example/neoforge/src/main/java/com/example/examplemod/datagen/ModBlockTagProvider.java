package com.example.examplemod.datagen;

import com.example.examplemod.Constants;
import com.example.examplemod.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

///  **Vanilla setup:** I think unlike the item tag, in vanilla this tag is useless,
/// but good riddance to have it for use in other mods anyway.
public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.SAPLINGS)
                .add(ModBlocks.SIMPLE_EXAMPLE_SAPLING.get())
                .add(ModBlocks.FANCY_EXAMPLE_SAPLING.get());
    }
}