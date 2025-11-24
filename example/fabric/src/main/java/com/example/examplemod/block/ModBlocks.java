package com.example.examplemod.block;

import com.example.examplemod.Constants;
import com.example.examplemod.worldgen.tree.ModTreeGrowers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/// **Vanilla setup:** here we register the block and assign the grower to the sapling.
public class ModBlocks {
    public static Block register(Block block, String name, boolean shouldRegisterItem) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);

        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Properties());
            Registry.register(BuiltInRegistries.ITEM, id, blockItem);
        }

        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    /// the properties can be copied off any sapling, their props are all the same.
    public static final Block SIMPLE_EXAMPLE_SAPLING = register(
            new SaplingBlock(ModTreeGrowers.SIMPLE_EXAMPLE_GROWER, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SAPLING)),
            "simple_example_sapling",
            true
    );

    public static final Block FANCY_EXAMPLE_SAPLING = register(
            new SaplingBlock(ModTreeGrowers.FANCY_EXAMPLE_GROWER, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SAPLING)),
            "fancy_example_sapling",
            true
    );

    public static void initialize() {}
}