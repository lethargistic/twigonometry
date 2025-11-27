package dev.maksiks.twigonometry.api;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

/**
 * A foliage placer requires to work with a wildcard foliage attachment.
 */
public class WildcardFoliagePlacer extends FoliagePlacer {
    public WildcardFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    public final class DummyFoliagePlacerType {
        public static final FoliagePlacerType<?> INSTANCE =
                new FoliagePlacerType<>(MapCodec.unit(() -> {
                    throw new UnsupportedOperationException(
                            "Twigonometry: Dummy foliage placer type cannot be deserialized."
                    );
                }));
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return DummyFoliagePlacerType.INSTANCE;
    }

    @Override
    public int foliageHeight(RandomSource randomSource, int i, TreeConfiguration treeConfiguration) {
        return 0;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, FoliageSetter setter,
                                 RandomSource random, TreeConfiguration config,
                                 int maxFreeTreeHeight, FoliageAttachment attachment,
                                 int foliageHeight, int foliageRadius, int offset) {
        //
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource randomSource, int i, int i1, int i2, int i3, boolean b) {
        return false;
    }

    public void createWildcardFoliage(LevelSimulatedReader level, FoliageSetter setter,
                                      RandomSource random, TreeConfiguration config,
                                      int maxFreeTreeHeight, WildcardFoliageAttachment attachment,
                                      int foliageHeight, int foliageRadius) {
        //
    }
}
