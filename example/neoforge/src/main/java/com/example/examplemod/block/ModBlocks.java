package com.example.examplemod.block;

import com.example.examplemod.Constants;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.worldgen.tree.ModTreeGrowers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/// **Vanilla setup:** here we register the block and assign the grower to the sapling.
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Constants.MOD_ID);
    
    /// the properties can be copied off any sapling, their props are all the same,
    /// except cherry has a different map color and sound.
    public static final DeferredBlock<Block> SIMPLE_EXAMPLE_SAPLING = registerBlock("simple_example_sapling",
            () -> new SaplingBlock(ModTreeGrowers.SIMPLE_EXAMPLE_GROWER, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SAPLING)));
    public static final DeferredBlock<Block> FANCY_EXAMPLE_SAPLING = registerBlock("fancy_example_sapling",
            () -> new SaplingBlock(ModTreeGrowers.FANCY_EXAMPLE_GROWER, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SAPLING)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {BLOCKS.register(eventBus);}
}