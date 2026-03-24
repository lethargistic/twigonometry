package dev.maksiks.twigonometry.internal

import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel

interface ITreePlacerContext {
    val level: WorldGenLevel
    val queue: PlacementQueue
    var random: RandomSource
}