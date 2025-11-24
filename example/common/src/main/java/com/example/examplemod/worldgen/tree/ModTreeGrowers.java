package com.example.examplemod.worldgen.tree;

import com.example.examplemod.Constants;
import com.example.examplemod.worldgen.ModConfiguredFeatures;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

///
/// **Vanilla setup:** here we create the tree growers alternatively known as sapling generators.
/// Their sole purpose is to tell the sapling block in the loader's respective ModBlocks class what tree to grow.
///
public class ModTreeGrowers {
    public static final TreeGrower SIMPLE_EXAMPLE_GROWER = new TreeGrower(Constants.MOD_ID + ":simple_example",
            Optional.empty(), Optional.of(ModConfiguredFeatures.SIMPLE_EXAMPLE_TREE_KEY), Optional.empty());
    public static final TreeGrower FANCY_EXAMPLE_GROWER = new TreeGrower(Constants.MOD_ID + ":fancy_example",
            Optional.empty(), Optional.of(ModConfiguredFeatures.FANCY_EXAMPLE_TREE_KEY), Optional.empty());
}