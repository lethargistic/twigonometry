package com.example.examplemod;


import com.example.examplemod.block.ModBlocks;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.worldgen.tree.ModFoliagePlacerTypes;
import com.example.examplemod.worldgen.tree.ModTrunkPlacerTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/// **Vanilla setup:** here we register our classes with Neo and initialize the mod
@Mod(Constants.MOD_ID)
public class ExampleMod {

    public ExampleMod(IEventBus eventBus) {
        NeoForge.EVENT_BUS.register(this);

        ModTrunkPlacerTypes.TRUNK_PLACER_TYPES.register(eventBus);
        ModFoliagePlacerTypes.FOLIAGE_PLACER_TYPES.register(eventBus);

        Shared.FANCY_TRUNK_SUPPLIER = ModTrunkPlacerTypes.FANCY_EXAMPLE_TRUNK_PLACER::get;
        Shared.SIMPLE_FOLIAGE_SUPPLIER = ModFoliagePlacerTypes.SIMPLE_EXAMPLE_FOLIAGE_PLACER::get;
        Shared.FANCY_FOLIAGE_SUPPLIER = ModFoliagePlacerTypes.FANCY_EXAMPLE_FOLIAGE_PLACER::get;

        ModItems.register(eventBus);
        ModBlocks.register(eventBus);

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}