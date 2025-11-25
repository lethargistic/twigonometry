package com.example.examplemod;

import com.example.examplemod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/// **Vanilla setup:** here we just add the saplings to a creative tab
public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final Supplier<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TAB.register("example_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.SIMPLE_EXAMPLE_SAPLING))
                    .title(Component.translatable("creativetab.examplemod.example"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.SIMPLE_EXAMPLE_SAPLING);
                        output.accept(ModBlocks.FANCY_EXAMPLE_SAPLING);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
