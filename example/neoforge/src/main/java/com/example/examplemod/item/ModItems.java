package com.example.examplemod.item;

import com.example.examplemod.Constants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/// **Vanilla setup:** we don't need anything here, just making it for the block item.
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}


