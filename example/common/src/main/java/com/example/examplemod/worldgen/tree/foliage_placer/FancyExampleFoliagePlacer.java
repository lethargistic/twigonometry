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
import dev.maksiks.twigonometry.api.*;
import dev.maksiks.twigonometry.api.LeafPlacerContext.HorizontalLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

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
/// ```
///
public class FancyExampleFoliagePlacer extends WildcardFoliagePlacer {
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
    public void createWildcardFoliage(
            LevelSimulatedReader level,
            FoliageSetter blockSetter,
            RandomSource random,
            TreeConfiguration config,
            int maxFreeTreeHeight,
            WildcardFoliageAttachment attachment,
            int foliageHeight,
            int foliageRadius
    ) {
        /// first of all we make a Twigonometry context, this is what we'll use for our placements
        LeafPlacerContext ctx = LeafPlacerContext.ctx(level, blockSetter, random, config, null, 100, false);
        /// you change these settings mid-way with a setter at any time btw, e.g.
        // ctx.setDebug(true);
        /// also, start the game with the Intellij debugger to not have to restart it on every change

        /// this is the starting position for placement
        /// here we return positions like we need to instead of whatever vanilla does
        /// so no need to lower it unlike in {@link SimpleExampleFoliagePlacer}
        BlockPos trunkPos = attachment.pos();

        /// here we take the values from the attachment
        /// passing the custom ones requires some hoops so we have to get them
        /// with the keys we setup earlier in the trunk placer
        ///
        /// each key must be declared right here, if an attachment is missing
        /// one of the keys or can't find a key on the list
        /// you'll get an error telling you what's missing
        attachment.require("not_branch", "branch_dir");
        boolean notBranch = attachment.getRequired("not_branch");
        Direction branchDir = attachment.getRequired("branch_dir");

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
        }

        // TODO example cur: make template example (this is temp)
        TemplatePlacerContext placerCtx = TemplatePlacerContext.ctx(ctx);

        ResourceLocation nbt = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "twigonometry/ghost_post");
        placerCtx.place(at.apply(curY), nbt);
        Constants.LOG.info("placing twigonometry/ghost_post.nbt e ");

        /// ctx.processQueue when you want to place the blocks is required
        /// for stepped placement, so block-by-block
        ctx.processQueue(1, null);

        /// that's it, as for the rest, the world's your canvas baiiii
    }

    /// this function places the lanterns on chains
    private ICustomLeafPlacer getLanternPlacer(LevelSimulatedReader level, FoliageSetter blockSetter, RandomSource random,
                                               LeafPlacerContext ctx, boolean placeLeaf) {
        return (pos, x, z, dist) -> {
            if (placeLeaf) ctx.placeLeaf(pos);
            safePlaceLanternHangie(ctx, level, pos, blockSetter, random);
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

            safePlaceLanternHangie(ctx, level, pos, blockSetter, random);
            placedHangie.set(true);
        };
    }

    private static void safePlaceLanternHangie(LeafPlacerContext ctx, LevelSimulatedReader level, BlockPos pos, FoliageSetter blockSetter, RandomSource random) {
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
        toPlace.forEach(pair -> ctx.placeSomethingElse(pair.getFirst(), pair.getSecond()));
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