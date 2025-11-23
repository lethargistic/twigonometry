package com.example.examplemod.worldgen.tree.foliage_placer;

import com.example.examplemod.Constants;
import com.example.examplemod.Shared;
import com.example.examplemod.worldgen.tree.trunk_placer.ExampleTrunkPlacer;
import com.mojang.datafixers.Products.P3;
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
import dev.maksiks.twigonometry.reference.ReferenceFoliage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

import java.util.List;
import java.util.function.Function;

import static dev.maksiks.twigonometry.api.LeafPlacerContextKt.HORIZONTAL_DIRECTIONS;

///
/// **Vanilla setup:** vanilla separates tree placement into two parts, a foliage placer for the leaves,
/// and a trunk placer ({@link ExampleTrunkPlacer}) for the logs.
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
public class ExampleFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<ExampleFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> blobParts(instance).apply(instance, ExampleFoliagePlacer::new));
    protected final int height;

    protected static <P extends ExampleFoliagePlacer> P3<Mu<P>, IntProvider, IntProvider, Integer> blobParts(Instance<P> instance) {
        return foliagePlacerParts(instance).and(Codec.intRange(0, 16).fieldOf("height").forGetter(p_68412_ -> p_68412_.height));
    }

    public ExampleFoliagePlacer(IntProvider radius, IntProvider offset, int height) {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return Shared.SHARED_EXAMPLE_FOLIAGE_PLACER;
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
        /// first of all we make Twigonometry context, this is what we'll use for our placements
        LeafPlacerContext ctx = LeafPlacerContext.ctx(level, blockSetter, random, config, null, false);

        /// this is the starting position for placement
        BlockPos trunkPos = attachment.pos();
        /// here we take the value from the attachment to determine if it's a branch or not
        boolean notBranch = !attachment.doubleTrunk();
        /// here we take the value from the attachment to determine the branch direction
        /// 0 = north, 1 = east, 2 = south, 3 = west
        int r = attachment.radiusOffset();
        Direction branchDir = HORIZONTAL_DIRECTIONS.get(r % 4);

        /// a small set of utils for all of this that I use, it's mostly just a single variable (curY) so you
        /// don't have to manage the distance from the bottom/top every time
        ///
        /// you can choose a different approach,
        /// e.g. making foliage in groups with data classes, but that is a lot more verbose
        ///
        Function<Integer, BlockPos> at = (h) -> trunkPos.above(h);
        Runnable bump = () -> curY += 1;
        Runnable lower = () -> curY -= 1;

        if (notBranch) {
            /// if it's not a branch we first place a diamond shape on top
            ///
            /// sometimes one of the pieces might be missing, tho making sure
            /// it never goes completely bald with guaranteed set to 75% (3/4 blocks here)
            // above
            {
                curY = 0;
                bump.run();
                HorizontalLayer layer = HorizontalLayer.create(95, 75, 100);
                ctx.incDiamond(at.apply(curY), 100, layer);
            }
            /// then two big diamonds in all directions except the branch direction
            ///
            /// notice how we pass in a list of positions, that makes it so all the
            /// chances only ever roll once and then positions of the list are
            /// places as identical copies
            // below

            /// this function places the lamps on chains
            ICustomLeafPlacer customLampPlacer = (pos, x, z, dist) -> {
                /// first we place thea actual leaf
                ctx.placeLeaf(pos);

                ///  then the chains and a lantern below
                int height = 2;
                if (random.nextInt(100) < 33) height = 3;
                int i;
                for (i = 1; i < height; i++) {
                    blockSetter.set(pos.below(i), Blocks.CHAIN.defaultBlockState());
                }
                blockSetter.set(pos.below(i+1), Blocks.LANTERN.defaultBlockState());
            };

            ICustomLeafPlacer customPlacer1 = (pos, x, z, dist) -> {
                blockSetter.set(pos, Blocks.REDSTONE_BLOCK.defaultBlockState());
                Constants.LOG.info("HIII");
            };

            curY = 0;
            lower.run();
            {
                /// a lantern hangie 50% of the time
                ICustomLeafPlacer maybeLampPlacer = random.nextBoolean()
                        ? customLampPlacer
                        : null;


                HorizontalLayer[] layers = {
                        HorizontalLayer.create(100),
                        HorizontalLayer.create(
                                100,
                                0,
                                100,
                                null,
                                false,
                                null,
                                Sector.getSectorByDirection(branchDir).getSkip()
                        )
                };
                ctx.incDiamond(List.of(at.apply(curY + 1), at.apply(curY)), 100, layers);
            }

            /// We also add a single block to a random side 50% of the time.
            ///
            /// however, 4 of the 12 blocks this would place are already there
            /// from the diamond we made just a second ago
            ///
            /// So we can use a LayerPattern to only keep the necessary blocks for a truly 50% chance
            /// forwards LayerPatterns mask - remove everything except for them, while,
            /// NOT LayerPatterns subtract - carve from existing blocks
            /// Sectors also subtract
            ///
            /// *internally they don't but that's easier to explain shhh*
            if (random.nextBoolean()) {
                /// also a lantern hangie 50% of the time
                ICustomLeafPlacer maybeLampPlacer = random.nextBoolean()
                        ? customLampPlacer
                        : null;

                HorizontalLayer[] layers = {
                        /// skipping the first outward layer since we don't need it
                        LeafPlacerContext.getEmpty(),
                        /// a 5x5 ring has 12 blocks, that -4, we need 1, 100/8=12.5~=13
                        /// positions are shuffled internally so we don't place on the same side every time,
                        /// so no need to worry about the placement chance here
                        ///
                        HorizontalLayer.create(
                                100,
                                13,
                                13,
                                null,
                                false,
                                LayerPattern.NOT_CARDINALS,
                                null,
                                maybeLampPlacer
                        )
                };

                ctx.incDisc(at.apply(curY), 0, true, layers);
            }

            /// and a diamond below,
            /// with 1 side missing sometimes
            lower.run();
            {
                HorizontalLayer layer = HorizontalLayer.create(75, 75, 100);
                ctx.incDiamond(at.apply(curY), 100, layer);
            }

    } else

    {
        /// if it's a branch we first place a simple diamond shape,
        /// then above we place a guaranteed center block and a small chance to add at maximum
        /// 2 more (50%) leaves on top. Also, the north hungers. We must not feed the north.
        curY = 0;
        ctx.diamond(at.apply(curY), 1, 0, 100);
        bump.run();
        HorizontalLayer layer = HorizontalLayer.create(
                15,
                0,
                100,
                null,
                false,
                null,
                Sector.N.getSkip());

        ctx.incDiamond(at.apply(curY), 100, layer);

        /// that's it, as for the rest, the world's your canvas bai
    }
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