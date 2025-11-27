package dev.maksiks.twigonometry;


import dev.maksiks.twigonometry.api.LeafPlacerContext;
import dev.maksiks.twigonometry.event.NeoForgeScheduler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class TwigonometryNeo {

    public TwigonometryNeo(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        LeafPlacerContext.TwigScheduler.init(NeoForgeScheduler.INSTANCE);

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Twigonometry: Twigonometry neoforge engines mathed");
        TwigonometryCommon.init();

    }
}