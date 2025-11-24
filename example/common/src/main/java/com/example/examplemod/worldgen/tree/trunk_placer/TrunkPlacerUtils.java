package com.example.examplemod.worldgen.tree.trunk_placer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class TrunkPlacerUtils {
    public static boolean placeLogOnAxis(Direction dir, LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos pos, TreeConfiguration config, Function<BlockState, BlockState> propertySetter) {
        if (TreeFeature.validTreePos(level, pos)) {
            blockSetter.accept(pos, propertySetter.apply(config.trunkProvider.getState(random, pos).setValue(RotatedPillarBlock.AXIS, dir.getAxis())));
            return true;
        } else {
            return false;
        }
    }
}
