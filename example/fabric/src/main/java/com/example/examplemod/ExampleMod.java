package com.example.examplemod;

import com.example.examplemod.block.ModBlocks;
import com.example.examplemod.worldgen.tree.ModFoliagePlacerTypes;
import com.example.examplemod.worldgen.tree.ModTreeGeneration;
import com.example.examplemod.worldgen.tree.ModTrunkPlacerTypes;
import net.fabricmc.api.ModInitializer;

public class ExampleMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        /// **Vanilla setup:** Here we initialize our block registry and creative tab.
        ModBlocks.initialize();
        ModCreativeModeTabs.initialize();

        /// Here we initialize our foliage placer type with Fabric's registry.
        ModFoliagePlacerTypes.initialize();
        ModTrunkPlacerTypes.initialize();

        /// And run the biome modification
        ModTreeGeneration.generateTrees();

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();
    }
}
