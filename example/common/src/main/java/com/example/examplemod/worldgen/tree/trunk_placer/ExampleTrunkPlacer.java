package com.example.examplemod.worldgen.tree.trunk_placer;

import com.example.examplemod.Shared;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.List;
import java.util.function.BiConsumer;

public class ExampleTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<ExampleTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
            p_70261_ -> trunkPlacerParts(p_70261_).apply(p_70261_, ExampleTrunkPlacer::new)
    );

    public ExampleTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return Shared.SHARED_EXAMPLE_TRUNK_PLACER;
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
        setDirtAt(level, blockSetter, random, pos.below(), config);

        int roll = random.nextInt(100);
        int variant =
            roll < 10 ? 2 : // round
            roll < 30 ? 1 : // smol
            0; // normie

        if (variant == 2 || variant == 0) {
            for (int i = 0; i < freeTreeHeight; i++) {
                this.placeLog(level, blockSetter, random, pos.above(i), config);
            }
        }
        if (variant == 1) {
            freeTreeHeight = 9 + random.nextInt(2);
            for (int i = 0; i < freeTreeHeight; i++) {
                this.placeLog(level, blockSetter, random, pos.above(i), config);
            }
        }

        //
        // Variant encoded in radiusOffset because that's enough access transformers for this week
        //
        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pos.above(freeTreeHeight), variant, false));
    }
}
