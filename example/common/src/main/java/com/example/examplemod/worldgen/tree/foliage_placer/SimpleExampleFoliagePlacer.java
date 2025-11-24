package com.example.examplemod.worldgen.tree.foliage_placer;

import com.example.examplemod.Constants;
import com.example.examplemod.Shared;
import com.example.examplemod.worldgen.tree.trunk_placer.FancyExampleTrunkPlacer;
import com.mojang.datafixers.Products.P3;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import dev.maksiks.twigonometry.api.ICustomLeafPlacer;
import dev.maksiks.twigonometry.api.LayerPattern;
import dev.maksiks.twigonometry.api.LeafPlacerContext;
import dev.maksiks.twigonometry.api.LeafPlacerContext.HorizontalLayer;
import dev.maksiks.twigonometry.api.Sector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static dev.maksiks.twigonometry.api.LeafPlacerContextKt.HORIZONTAL_DIRECTIONS;

///
/// **Vanilla setup:** vanilla separates tree placement into two parts, a foliage placer for the leaves,
/// and a trunk placer ({@link FancyExampleTrunkPlacer}) for the logs.
///
/// You can take a look at
/// - {@link net.minecraft.world.level.levelgen.feature.trunkplacers}
/// - {@link net.minecraft.world.level.levelgen.feature.foliageplacers}
///
/// for more info and examples on vanilla placers.
///
/// **Twigonometry:** this is where Twigonometry comes in, to begin making your foliage create a context. Take a look at the docstrings inside its class for more info.
/// ```
/// LeafPlacerContext.ctx(level, blockSetter, random, config, null, false)
///```
///
public class SimpleExampleFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<SimpleExampleFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> blobParts(instance).apply(instance, SimpleExampleFoliagePlacer::new));
    protected final int height;

    protected static <P extends SimpleExampleFoliagePlacer> P3<Mu<P>, IntProvider, IntProvider, Integer> blobParts(Instance<P> instance) {
        return foliagePlacerParts(instance).and(Codec.intRange(0, 16).fieldOf("height").forGetter(p_68412_ -> p_68412_.height));
    }

    public SimpleExampleFoliagePlacer(IntProvider radius, IntProvider offset, int height) {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return Shared.SHARED_SIMPLE_EXAMPLE_FOLIAGE_PLACER;
    }

    /// current vertical position, used later
    private Integer curY;

    @Override
    protected void createFoliage(
            LevelSimulatedReader level,
            FoliageSetter blockSetter,
            RandomSource random,
            TreeConfiguration config,
            int maxFreeTreeHeight,
            FoliageAttachment attachment,
            int foliageHeight,
            int foliageRadius,
            int offset
    ) {
        /// first of all we make a Twigonometry context, this is what we'll use for our placements
        LeafPlacerContext ctx = LeafPlacerContext.ctx(level, blockSetter, random, config, null, false);

        /// this is the top of the trunk, vanilla trunk placers return it one block above it so we lower it
        BlockPos trunkPos = attachment.pos().below();

        /// these are small set of utils that I use, it's just state tracking so you
        /// don't have to think of the distance from the bottom/top every time
        ///
        /// you can choose a different approach,
        /// e.g. making foliage in groups with data classes, or a method for each logical section,
        /// but that is a lot more verbose, even if probably more readable
        Function<Integer, BlockPos> at = (h) -> trunkPos.above(h);
        Runnable bump = () -> curY += 1;
        Runnable lower = () -> curY -= 1;

        /// we reset the position to 0
        // above
        curY = 0;
        /// each time we bump it goes up by 1
        bump.run();
        /// here we place a diamond (rhombus, square rotated 45°)
        ctx.diamond(at.apply(curY), 1);
        /// then we go up
        bump.run();
        {
            /// To define any complex placement we use an incremental shape
            /// these are placed outwards from their center.
            /// I recommend playing around with these and seeing source docstrings for more info.
            /// A single layer = a single expansion outwards symmetrically. So a square with 4 layers
            /// would be 5x5, 1 is the center block controlled by centerChance so that's not a layer.
            /// then by applying chances, patterns, sectors and other features
            /// you can generate whatever tree you want.
            HorizontalLayer layer = HorizontalLayer.create(100, 25, 25);
            ctx.incDiamond(at.apply(curY), 100, layer);
        }
        /// the rest is applying all that.
        bump.run();
        ctx.placeLeaf(at.apply(curY));

        // below
        curY = 0;
        ctx.diamond(at.apply(curY), 1, 0);
        lower.run();
        {
            ctx.diamond(at.apply(curY), 1, 0);
            HorizontalLayer layer = HorizontalLayer.create(
                    100,
                    25,
                    25,
                    null,
                    false,
                    LayerPattern.CORNERS);
            ctx.incSquare(at.apply(curY), 0, layer);
        }
        lower.run();
        ctx.square(at.apply(curY), 1, 0);
        lower.run();
        ctx.diamond(at.apply(curY), 2, 0);
        lower.run();
        {
            HorizontalLayer[] layers = {
                    HorizontalLayer.create(100),
                    HorizontalLayer.create(50, 75, 100)
            };
            ctx.incDiamond(at.apply(curY), 0, layers);
        }
        lower.run();
        ctx.diamond(at.apply(curY), 1, 0);
    }

    @Override
    public int foliageHeight(RandomSource random, int height, TreeConfiguration config) {
        return this.height;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        return localX == range && localZ == range && (random.nextInt(2) == 0 || localY == 0);
    }
}