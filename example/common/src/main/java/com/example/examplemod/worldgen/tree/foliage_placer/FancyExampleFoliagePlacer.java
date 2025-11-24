package com.example.examplemod.worldgen.tree.foliage_placer;

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
public class FancyExampleFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<FancyExampleFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> blobParts(instance).apply(instance, FancyExampleFoliagePlacer::new));
    protected final int height;

    protected static <P extends FancyExampleFoliagePlacer> P3<Mu<P>, IntProvider, IntProvider, Integer> blobParts(Instance<P> instance) {
        return foliagePlacerParts(instance).and(Codec.intRange(0, 16).fieldOf("height").forGetter(p_68412_ -> p_68412_.height));
    }

    public FancyExampleFoliagePlacer(IntProvider radius, IntProvider offset, int height) {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return Shared.FANCY_FOLIAGE_SUPPLIER.get();
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

        /// this is the starting position for placement
        /// here we return positions like we need to instead of vanilla does
        /// so no need to lower it unlike in {@link SimpleExampleFoliagePlacer}
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
            curY = 0;
            bump.run();
            {
                HorizontalLayer layer = HorizontalLayer.create(95, 75, 100);
                ctx.incDiamond(at.apply(curY), 100, layer);
            }
            /// then two big diamonds in all directions except the branch direction
            ///
            /// notice how we pass in a list of positions, that makes it so all the
            /// chances only ever roll once and then positions of the list are
            /// places as identical copies
            // below
            curY = 0;
            lower.run();
            {
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

                /// at the same height we also place a lantern hangie sometimes.
                /// here to not place it a spare time we also skip the leaf, but really it doesn't matter
                /// only saves you nanoseconds of computation time
                /// Direction here doesn't matter so passing in whatever
                ICustomLeafPlacer maybeOneDirLanternPlacer = random.nextInt(100) < 33
                        ? getCustomOneDirLanternPlacer(level, blockSetter, random, ctx, Direction.NORTH, false)
                        : null;
                HorizontalLayer layer =
                        HorizontalLayer.create(
                                100,
                                0,
                                100,
                                null,
                                false,
                                LayerPattern.CORNERS,
                                null,
                                maybeOneDirLanternPlacer
                        );
                ctx.incSquare(at.apply(curY), 0, layer);
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
                ICustomLeafPlacer maybeLanternPlacer = random.nextBoolean()
                        ? getLanternPlacer(level, blockSetter, random, ctx, true)
                        : null;

                HorizontalLayer[] layers = {
                        /// skipping the first outward layer since we don't need it
                        LeafPlacerContext.getEmpty(),
                        /// a 5x5 ring has 12 blocks, that -4, we need 1, 100/8=12.5~=13
                        /// positions are shuffled internally so we don't place on the same side every time,
                        /// so no need to worry about the placement chance here
                        HorizontalLayer.create(
                                100,
                                13,
                                13,
                                null,
                                false,
                                LayerPattern.NOT_CARDINALS,
                                null,
                                maybeLanternPlacer
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

        } else {
            /// a 33% chance to place a lantern hangie below one of the blocks
            /// but only once, and also not in the opposite to the branch direction
            /// since that would be between the branch and the trunk
            ICustomLeafPlacer maybeOneDirLanternPlacer = random.nextInt(100) < 33
                    ? getCustomOneDirLanternPlacer(level, blockSetter, random, ctx, branchDir, true)
                    : null;

            /// if it's a branch we first place a simple diamond shape with lanterns below,
            /// then above we place a guaranteed center block and a small chance to add at maximum
            /// 2 more (50%) leaves on top. Also, the north hungers. We must not feed the north.
            curY = 0;
            {
                /// without lanterns this would be equivalent to ctx.diamond(at.apply(curY), 1, 0);
                HorizontalLayer layer = HorizontalLayer.create(
                        100,
                        100,
                        100,
                        null,
                        false,
                        null,
                        null,
                        maybeOneDirLanternPlacer
                );
                ctx.incDiamond(at.apply(curY), 0, layer);
            }
            bump.run();
            {
                HorizontalLayer layer = HorizontalLayer.create(
                        15,
                        0,
                        100,
                        null,
                        false,
                        null,
                        Sector.N.getSkip(),
                        maybeOneDirLanternPlacer
                );

                ctx.incDiamond(at.apply(curY), 100, layer);
            }

            /// that's it, as for the rest, the world's your canvas baiiii
        }
    }

    /// this function places the lanterns on chains
    private ICustomLeafPlacer getLanternPlacer(LevelSimulatedReader level, FoliageSetter blockSetter, RandomSource random,
                                               LeafPlacerContext ctx, boolean placeLeaf) {
        return (pos, x, z, dist) -> {
            if (placeLeaf) ctx.placeLeaf(pos);
            safePlaceLanternHangie(level, pos, blockSetter, random);
        };
    }

    private @NotNull ICustomLeafPlacer getCustomOneDirLanternPlacer(LevelSimulatedReader level, FoliageSetter blockSetter, RandomSource random, LeafPlacerContext ctx,
                                                                    Direction branchDir, boolean placeLeaf) {
        /// atomic because that's how lambdas work
        AtomicBoolean placedHangie = new AtomicBoolean(false);
        /// here x and z are relative placement coords,
        /// so x=-1 & z=1 means the placement
        /// is 1 block away on the -x axis and 1 away on +z axis
        /// and dist is the index of the HorizontalLayer
        return (pos, x, z, dist) -> {
            if (placeLeaf) ctx.placeLeaf(pos);

            /// here we also check if it already placed, if yes then we skip
            if (placedHangie.get()) return;

            /// here we check if it's not the opposite direction of the branch
            Direction opposite = branchDir.getOpposite();
            if ((opposite.getStepX() != 0 && x == opposite.getStepX()) ||
                    (opposite.getStepZ() != 0 && z == opposite.getStepZ())) {
                return;
            }

            safePlaceLanternHangie(level, pos, blockSetter, random);
            placedHangie.set(true);
        };
    }

    private static void safePlaceLanternHangie(LevelSimulatedReader level, BlockPos pos, FoliageSetter blockSetter, RandomSource random) {
        /// sometimes 1 chain, sometimes 2 chains
        int chainHeight = 1;
        if (random.nextInt(100) < 33) chainHeight = 2;

        List<Pair<BlockPos, BlockState>> toPlace = new ArrayList<>();

        int i;
        for (i = 0; i < chainHeight; ++i) {
            BlockPos placePos = pos.below(i + 1);
            /// if we're replacing anything other than air then we stop
            /// this shouldn't happen much anyway but
            /// 1) we don't want to replace bedrock (more explicitly an isSolid check would be better)
            /// 2) even if it's grass or something replaceable, that just means it's too close to the ground anyway
            if (!level.isStateAtPosition(placePos, BlockBehaviour.BlockStateBase::isAir)) {
                return;
            }
            toPlace.add(Pair.of(placePos, Blocks.CHAIN.defaultBlockState()));
        }
        toPlace.add(
                Pair.of(pos.below(i + 1),
                        Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true)));
        toPlace.forEach(pair -> blockSetter.set(pair.getFirst(), pair.getSecond()));
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