package com.example.examplemod.worldgen.tree.trunk_placer;

import com.example.examplemod.Shared;
import com.example.examplemod.worldgen.tree.foliage_placer.FancyExampleFoliagePlacer;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static dev.maksiks.twigonometry.api.LeafPlacerContextKt.HORIZONTAL_DIRECTIONS;

///
/// **Vanilla setup:** vanilla separates tree placement into two parts, a trunk placer for the logs,
/// and a foliage placer ({@link FancyExampleFoliagePlacer}) for the leaves.
///
/// You can take a look at
/// - {@link net.minecraft.world.level.levelgen.feature.trunkplacers}
/// - {@link net.minecraft.world.level.levelgen.feature.foliageplacers}
///
/// for more info and examples on vanilla placers.
///
/// **Twigonometry:** currently Twigonometry doesn't add any features for trunk placement (despite the name lmao),
/// tho they're coming soon/eventually. If you want to use the same features as the leaf placer you can create a context
/// with custom foliage set to your log.
///
public class FancyExampleTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<FancyExampleTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
            p_70261_ -> trunkPlacerParts(p_70261_).apply(p_70261_, FancyExampleTrunkPlacer::new)
    );

    public FancyExampleTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return Shared.FANCY_TRUNK_SUPPLIER.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
            LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> blockSetter,
            RandomSource random,
            int freeTreeHeight,
            BlockPos pos,
            TreeConfiguration config
    ) {
        /// here we set a dirt block under the tree,
        /// vanilla does this for most trees to replace the grass block below
        setDirtAt(level, blockSetter, random, pos.below(), config);

        /// our foliage placer will be run once per each foliage attachment,
        /// they tell it where to start placing and some extra data
        List<FoliagePlacer.FoliageAttachment> attachments = new ArrayList<>();

        /// freeTreeHeight is a variable vanilla calculates internally from the values
        /// passed into the constructor, you may choose to ignore it
        int trunkHeight = freeTreeHeight;

        /// here we pick the location to place the branch at,
        /// either block 3 or 4
        int branchY = 2 + random.nextInt(2);

        /// if we get a higher branch, then we move the foliage up so we don't collide
        /// with the leaves
        if (branchY == 3) trunkHeight += 1;

        /// branch direction
        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);

        /// the loop places each vertical trunk block,
        /// at the height we chose previously we place our branch
        for (int i = 0; i < trunkHeight; i++) {
            /// pos is the origin of the trunk, we offset it to be above our height
            BlockPos placementPos = pos.above(i);
            this.placeLog(level, blockSetter, random, placementPos, config);

            if (i == branchY) {
                /// 1 block to the side
                BlockPos cursor = placementPos.relative(dir);
                TrunkPlacerUtils.placeLogOnAxis(dir, level, blockSetter, random, cursor, config, Function.identity());
                /// 1 more block to the side, 1 block up
                cursor = cursor.relative(dir).above();
                this.placeLog(level, blockSetter, random, cursor, config);

                /// passing the attachment onto the trunk
                attachments.add(new FoliagePlacer.FoliageAttachment(cursor, HORIZONTAL_DIRECTIONS.indexOf(dir), true));
            }
        }

        /// you can use radiusOffset or doubleTrunk as a jank way to encode tree variants or any other data passed onto the leaf placer
        /// in this case it's main foliage - doubleTrunk = false or branch - doubleTrunk = true (see {@link FancyExampleFoliagePlacer})
        attachments.add(new FoliagePlacer.FoliageAttachment(pos.above(trunkHeight-1), HORIZONTAL_DIRECTIONS.indexOf(dir), false));
        return attachments;
    }

}
