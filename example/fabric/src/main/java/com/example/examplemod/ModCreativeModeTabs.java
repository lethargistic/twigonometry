package com.example.examplemod;

import com.example.examplemod.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/// **Vanilla setup:** here we just add the saplings to a creative tab
public class ModCreativeModeTabs {
    public static final CreativeModeTab EXAMPLE_ITEM_GROUP = register("example_tab",
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.FANCY_EXAMPLE_SAPLING))
                    .title(Component.translatable("creativetab.examplemod.example"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.SIMPLE_EXAMPLE_SAPLING);
                        output.accept(ModBlocks.FANCY_EXAMPLE_SAPLING);
                    })
                    .build());

    private static CreativeModeTab register(String name, CreativeModeTab tab) {
        return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                tab);
    }

    public static void initialize() {
    }
}
