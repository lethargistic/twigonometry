package dev.maksiks.twigonometry.api

import dev.maksiks.twigonometry.api.LayerPattern.Companion.matchesPattern
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.tags.BlockTags
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelSimulatedReader
import net.minecraft.world.level.LevelWriter
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

@JvmField
val DIAGONALS: Array<Direction> = arrayOf(
    Direction.NORTH, Direction.EAST,
    Direction.NORTH, Direction.WEST,
    Direction.SOUTH, Direction.EAST,
    Direction.SOUTH, Direction.WEST
)

@JvmField
val HORIZONTAL_DIRECTIONS = Direction.Plane.HORIZONTAL.toList()
@JvmField
val VERTICAL_DIRECTIONS = Direction.Plane.VERTICAL.toList()


// TODO: Twigonometry: java examples for everything, simpler docs, and more docs

/**
 *
 * ### Config for foliage placement.
 *
 * The idea is using shapes with configurable placement chances to
 * sculpt your tree layer per layer or en masse.
 *
 * @see square
 * @see incSquare
 * @see diamond
 * @see incDiamond
 * @see disc
 * @see incDisc
 * @see Sector
 * @see LayerPattern
 **/
class LeafPlacerContext(
    var level: LevelSimulatedReader,
    var blockSetter: FoliagePlacer.FoliageSetter,
    var random: RandomSource,
    var config: TreeConfiguration,
    var foliage: Array<BlockState>? = null,
    var debug: Boolean = false
) {
    companion object {
        @JvmStatic
        fun ctx(
            level: LevelSimulatedReader,
            blockSetter: FoliagePlacer.FoliageSetter,
            random: RandomSource,
            config: TreeConfiguration,
            foliage: Array<BlockState>? = null,
            debug: Boolean = false
        ): LeafPlacerContext {
            return LeafPlacerContext(level, blockSetter, random, config, foliage, debug)
        }

        @JvmStatic
        val empty = HorizontalLayer(0)

        @JvmStatic
        val simpleGuaranteed = HorizontalLayer(100)

        @JvmStatic
        val skipCardinalSectors = Sector.N.skip or Sector.S.skip or Sector.E.skip or Sector.W.skip
        val skipDiagonalSectors = Sector.NE.skip or Sector.NW.skip or Sector.SE.skip or Sector.SW.skip
    }

    fun placeLeaf(pos: BlockPos) {
        // cutting out the try part just to separate it tho it does the same thing
        FoliagePlacer.tryPlaceLeaf(level, blockSetter, random, config, pos)
    }

    /**
     * Safely removes a leaf block
     **/
    fun removeLeaf(removePos: BlockPos) {
        if (isCurrentFoliage(removePos) || isLeaves(removePos)
            && !level.isStateAtPosition(removePos) { it.isAir }
        ) {
            blockSetter.set(removePos, Blocks.AIR.defaultBlockState())
        }
    }

    /**
     * A horizontal layer
     *
     * @param chance chance of spawning a block in this layer, 1-100%.
     * @param guaranteed guaranteed percentage of blocks to spawn in this layer (accounted for other params), 1-100%.
     * @param cap maximum percentage of blocks to spawn in this layer (accounted for other params), 1-100%.
     * @param centricFactor biases blocks to spawn near the middle/corners.
     * - 1.0 - higher chance towards the middle (axes);
     * - 0.5 normal;
     * - 0.0 higher chance towards the corners (sides).
     * @param removeIfDecays if true, will ensure that each leaf block is connected to a log in the near 7 blocks,
     *  same as the vanilla condition for leaf decay.
     *  This just removes the unconnected leaves, you can substitute for that by slightly increasing [chance].
     * @param custom you can pass in your own code to be executed instead of the leaf placing function.
     *  Can be useful if you want to add custom conditions or place different blocks.
     *
     * E.g., place a lantern below every other block in horizontal layer 2:
     * ```
     * LeafPlacerContext.HorizontalLayer(100, custom = { ctx, pos, x, z, dist ->
     *     if (dist == 2 && ctx.random.nextBoolean()) {
     *         ctx.placeLeaf(pos)
     *         ctx.blockSetter.set(pos.below(), Blocks.LANTERN.defaultBlockState())
     *     }
     * })
     * ```
     * If you don't care about the horizontal layer and don't need any of the other [HorizontalLayer] placement features,
     * it's likely better to make a separate for loop to place blocks instead.
     *
     *  @see incSquare
     *  @see incDiamond
     *  @see incDisc
     * */
    data class HorizontalLayer(
        val chance: Int,
        val guaranteed: Int = 0,
        val cap: Int = 100,
        val centricFactor: Double? = null,
        val removeIfDecays: Boolean = false,
        val pattern: LayerPattern? = null,
        val skipSector: Int? = null,
        val custom: ICustomLeafPlacer? = null
    ) {
        companion object {
            /**
             * On Java always use this method so you have overloads
             *
             * On Kotlin you can use the constructor just fine. Glory to optional parameters! For the emperor!
             */
            @JvmStatic
            @JvmOverloads
            fun create(
                chance: Int,
                guaranteed: Int = 0,
                cap: Int = 100,
                centricFactor: Double? = null,
                removeIfDecays: Boolean = false,
                pattern: LayerPattern? = null,
                skipSector: Int? = null,
                custom: ICustomLeafPlacer? = null
            ): HorizontalLayer {
                return HorizontalLayer(chance, guaranteed, cap, centricFactor, removeIfDecays, pattern, skipSector, custom)
            }
        }
    }

    data class Candidate(
        val placePos: BlockPos,
        val dist: Int,
        val x: Int,
        val z: Int
    )

    /**
     * Checks if a block is a foliage block.
     *
     * @see foliage
     * */
    private fun isCurrentFoliage(pos: BlockPos): Boolean {
        if (foliage == null) {
            return false
        }
        for (state in foliage) {
            if (level.isStateAtPosition(pos) { it.`is`(state.block) }) return true
        }
        return false
    }

    private fun isLeaves(pos: BlockPos): Boolean {
        return level.isStateAtPosition(pos) { it.`is`(BlockTags.LEAVES) }
    }

    // basically same as vanilla
    private fun wouldDecay(pos: BlockPos): Boolean {
        val visited = mutableSetOf<BlockPos>()
        val queue = ArrayDeque<Pair<BlockPos, Int>>()
        queue.add(pos to 0)

        while (queue.isNotEmpty()) {
            val (current, dist) = queue.removeFirst()

            if (dist >= 7) continue
            if (!visited.add(current)) continue

            for (dir in Direction.entries) {
                val neighbor = current.relative(dir)
                if (neighbor in visited) continue

                if (level.isStateAtPosition(neighbor) { it.`is`(BlockTags.LOGS) }) {
                    return false
                }

                if (isCurrentFoliage(neighbor) || isLeaves(neighbor)) {
                    queue.add(neighbor to dist + 1)
                }
            }
        }

        return true
    }

    private enum class ShapeType {
        SQUARE,
        DIAMOND,
        DISC
    }

    private fun calculateDist(shapeType: ShapeType, x: Int, z: Int): Int {
        return when (shapeType) {
            ShapeType.SQUARE -> maxOf(abs(x), abs(z))
            ShapeType.DIAMOND -> abs(x) + abs(z)
            ShapeType.DISC -> sqrt((x * x + z * z).toDouble()).toInt()
        }
    }

    /**
     * Internal shape building math, use [incSquare], [incDiamond], [incDisc] instead.
     *
     * @see incSquare
     * @see incDiamond
     * @see incDisc
     * */
    private fun incShape(
        positions: Iterable<BlockPos>,
        centerChance: Int = 100,
        shapeType: ShapeType,
        vararg layers: HorizontalLayer,
        discSmoothInternal: Boolean = true
    ) {
        val radius = layers.size
        val basePos = positions.firstOrNull() ?: throw IllegalArgumentException("Empty position")
        val finalCandidates = mutableListOf<Candidate>()

        val layerCandidates = Array(radius) { mutableListOf<Candidate>() }
        val layerTotals = IntArray(radius) { 0 }

        // disc cutoff
        val outerCutoff = if (shapeType == ShapeType.DISC) radius + 0.4 else null

        // first pass, skeleton
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                val dist = calculateDist(shapeType, x, z)

                if (shapeType == ShapeType.DISC) {
                    if (outerCutoff == null) throw IllegalStateException("outerCutoff is null for DISC shape")
                    val distSq = x * x + z * z
                    if (!discSmoothInternal && distSq > radius * radius) continue
                    if (discSmoothInternal && distSq > outerCutoff * outerCutoff) continue
                    val euclideanDist = sqrt((x * x + z * z).toDouble()).toInt()
                    if (euclideanDist > radius) continue
                } else {
                    if (dist > radius) continue
                }

                if (dist > 0) {
                    val layer = layers[dist - 1]

                    // matching to pattern
                    if (layer.pattern != null && !matchesPattern(layer.pattern, x, z, dist, radius)) {
                        continue
                    }

                    // skipping sectors
                    if (layer.skipSector != null) {
                        val angle = atan2(z.toDouble(), x.toDouble())
                        val normalizedAngle = if (angle < 0) angle + 2 * PI else angle
                        val octant = ((normalizedAngle + PI / 2) / (PI / 4)).toInt() % 8
                        val octantBit = 1 shl octant

                        if ((layer.skipSector and octantBit) != 0) continue
                    }

                    layerTotals[dist - 1]++
                }
            }
        }

        // second pass, collecting all potential candidates with their chances
        val layerPotentialCandidates = Array(radius) { mutableListOf<Pair<Candidate, Int>>() }

        for (x in -radius..radius) {
            for (z in -radius..radius) {
                val dist = calculateDist(shapeType, x, z)

                // same filtering as in pass 1
                if (shapeType == ShapeType.DISC) {
                    if (outerCutoff == null) throw IllegalStateException("outerCutoff is null for DISC shape")
                    val distSq = x * x + z * z
                    if (!discSmoothInternal && distSq > radius * radius) continue
                    if (discSmoothInternal && distSq > outerCutoff * outerCutoff) continue
                    val euclideanDist = sqrt((x * x + z * z).toDouble()).toInt()
                    if (euclideanDist > radius) continue
                } else {
                    if (dist > radius) continue
                }

                val placePos = basePos.offset(x, 0, z)

                if (dist == 0) {
                    if (random.nextInt(100) < centerChance) {
                        finalCandidates.add(Candidate(placePos, dist, x, z))
                    }
                } else {
                    val layer = layers[dist - 1]

                    // matching to pattern
                    if (layer.pattern != null && !matchesPattern(layer.pattern, x, z, dist, radius)) {
                        continue
                    }

                    // skipping sectors
                    if (layer.skipSector != null) {
                        val angle = atan2(z.toDouble(), x.toDouble())
                        val normalizedAngle = if (angle < 0) angle + 2 * PI else angle
                        val octant = ((normalizedAngle + PI / 2) / (PI / 4)).toInt() % 8
                        val octantBit = 1 shl octant

                        if ((layer.skipSector and octantBit) != 0) continue
                    }


                    // calculating base chance with centricFactor
                    val baseChance = if (layer.centricFactor != null) {
                        val gradient = when (shapeType) {
                            ShapeType.SQUARE -> {
                                val layerEdgeDist = dist - (abs(x).coerceAtMost(abs(z)))
                                1.0 - (layerEdgeDist.toDouble() / dist)
                            }

                            ShapeType.DIAMOND -> {
                                1.0 - abs(abs(x) - abs(z)).toDouble() / dist
                            }

                            ShapeType.DISC -> {
                                val angle = atan2(z.toDouble(), x.toDouble())
                                val normalizedAngle = (angle % (PI / 2)) / (PI / 2)
                                1.0 - 2.0 * abs(normalizedAngle - 0.5)
                            }
                        }

                        val factor = layer.centricFactor
                        val bias = (0.5 - factor) * 2.0
                        val weighted = gradient * (1 + bias) + (1 - gradient) * (1 - bias)
                        val modifiedChance = (layer.chance * weighted).coerceIn(0.0, 100.0)
                        modifiedChance.toInt()
                    } else {
                        layer.chance
                    }

                    val candidate = Candidate(placePos, dist, x, z)
                    layerPotentialCandidates[dist - 1].add(candidate to baseChance)
                }
            }
        }

        // third pass, selecting candidates per layer w guaranteed/cap
        for (layerIndex in 0 until radius) {
            val layer = layers[layerIndex]
            val potentials = layerPotentialCandidates[layerIndex]
            val totalInLayer = layerTotals[layerIndex]

            val guaranteedCount = (totalInLayer * layer.guaranteed / 100.0).toInt()
            val capCount = (totalInLayer * layer.cap / 100.0).toInt()

            // gambling
            val selected = mutableListOf<Candidate>()
            for ((candidate, chance) in potentials) {
                if (random.nextInt(100) < chance) {
                    selected.add(candidate)
                }
            }

            if (selected.size < guaranteedCount) {
                val unselected = potentials.map { it.first }.filter { it !in selected }.toMutableList()
                unselected.shuffle()

                val needed = guaranteedCount - selected.size
                selected.addAll(unselected.take(needed))
            }

            if (selected.size > capCount) {
                selected.shuffle()
                selected.subList(capCount, selected.size).clear()
            }

            layerCandidates[layerIndex].addAll(selected)
            finalCandidates.addAll(selected)
        }

        // replicating the positions for each copy
        if (positions.count() > 1) {
            val others = positions.drop(1)
            val clones = mutableListOf<Candidate>()
            for (candidate in finalCandidates) {
                for (other in others) {
                    val dx = other.x - basePos.x
                    val dy = other.y - basePos.y
                    val dz = other.z - basePos.z
                    clones.add(candidate.copy(placePos = candidate.placePos.offset(dx, dy, dz)))
                }
            }
            finalCandidates.addAll(clones)
        }

        if (this.debug) {
            for (layerIndex in 0 until radius) {
                val potentials = layerPotentialCandidates[layerIndex]

                for ((candidate, chance) in potentials) {
                    val armorStand = EntityType.ARMOR_STAND.create(level as Level)
                    armorStand?.let {
                        it.setPos(candidate.placePos.x + 0.5, candidate.placePos.y + 0.5, candidate.placePos.z + 0.5)
                        it.isInvisible = true
                        it.isMarker = true
                        it.customName = Component.literal("$chance%")
                        it.isCustomNameVisible = true
                        (level as LevelWriter).addFreshEntity(it)
                    }
                }
            }
        }

        // placing all selected candidates
        for (candidate in finalCandidates) {
            val dist = candidate.dist
            if (dist == 0) {
                placeLeaf(candidate.placePos)
            } else {
                layers[dist - 1].custom?.place(candidate.placePos, candidate.x, candidate.z, dist)
                    ?: placeLeaf(candidate.placePos)
            }
        }

        // carving (decay check)
        for (candidate in finalCandidates) {
            val dist = candidate.dist
            if (dist == 0) continue

            val layer = layers[dist - 1]
            if (layer.removeIfDecays && wouldDecay(candidate.placePos)) {
                removeLeaf(candidate.placePos)
            }
        }
    }

    /**
     * See docs for similar overload taking in multiple BlockPos.
     * */
    private fun incShape(
        pos: BlockPos,
        centerChance: Int = 100,
        shapeType: ShapeType,
        vararg layers: HorizontalLayer,
        discSmoothInternal: Boolean = true
    ) = incShape(listOf(pos), centerChance, shapeType, *layers, discSmoothInternal = discSmoothInternal)

    /**
     * Makes a square of a certain radius at a certain position.
     *
     * @param radius radius.
     * @param pos the position from which placing starts. Usu. above/at/below trunk attachment position.
     * @param centerChance placement chance specifically for the center block, 1-100%.
     * @param chance chance of placing each individual block, 1-100%.
     *
     * See [incDisc] and [incShape] for more details
     *
     * @see incDisc
     * @see incShape
     * */
    @JvmOverloads
    fun square(
        pos: BlockPos,
        radius: Int,
        centerChance: Int = 100,
        chance: Int = 100
    ) = incSquare(pos, centerChance, *Array(radius) { HorizontalLayer(chance) })

    /**
     * See docs for similar overload taking in a single BlockPos.
     *
     * @param positions an iterable of positions, placement starts at the first position, chances only proc once,
     *  then the shape is copied over to all other positions.
     *  Useful for making shapes extend themselves without running the chances again.
     * */
    @JvmOverloads
    fun square(
        positions: Iterable<BlockPos>,
        radius: Int,
        centerChance: Int = 100,
        chance: Int = 100
    ) = incSquare(positions, centerChance, *Array(radius) { HorizontalLayer(chance) })


    /**
     * Makes a square with incremental chances for each layer.
     *
     * For example (Java):
     * ```
     * ctx.incSquare(pos, 100, new HorizontalLayer(50), new HorizontalLayer(25));
     * ```
     * (Kotlin):
     * ```
     * ctx.incSquare(pos, 100, HorizontalLayer(50), HorizontalLayer(25))
     * ```
     * Would result in:
     *  ```
     *  🏿 🏿 🏿 🏿 🏿 ️
     *  🏿 🏾 🏾 🏾 🏿
     *  🏿 🏾 🏽 🏾 🏿
     *  🏿 🏾 🏾 🏾 🏿
     *  🏿 🏿 🏿 🏿 🏿
     *  ```
     *  Where the chances of spawning a block are:
     *  🏽 - 100%; 🏾 - 50%; 🏿 - 25%;
     *
     * @param pos the position from which placing starts. Usu. above/below trunk attachment position.
     * @param centerChance chance specifically for the center block,
     *  separate from [layers] because all other [HorizontalLayer] params don't really apply to it,
     *  and you might want to alternate it quite often.
     * @param layers an array of [HorizontalLayer], each defines settings for a single incremented outward layer,
     *  and the size of the array defines the radius of the resulting shape.
     *
     * @see square
     * @see incShape
     * */
    @JvmOverloads
    fun incSquare(pos: BlockPos, centerChance: Int = 100, vararg layers: HorizontalLayer) =
        incShape(listOf(pos), centerChance, ShapeType.SQUARE, *layers)

    /**
     * See docs for similar overload taking in a single BlockPos.
     *
     * @param positions an iterable of positions, placement starts at the first position, chances only proc once,
     *  then the shape is copied over to all other positions.
     *  Useful for making shapes extend themselves without running the chances again.
     * */
    @JvmOverloads
    fun incSquare(positions: Iterable<BlockPos>, centerChance: Int = 100, vararg layers: HorizontalLayer) =
        incShape(positions, centerChance, ShapeType.SQUARE, *layers)

    /**
     * Makes a diamond shape of a certain radius at a certain position.
     *
     * @param radius radius.
     * @param pos the position from which placing starts. Usu. above/at/below trunk attachment position.
     * @param centerChance placement chance specifically for the center block, 1-100%.
     * @param chance chance of placing each individual block, 1-100%.
     *
     * See [incDiamond] and [incShape] for more details
     *
     * @see incDiamond
     * @see incShape
     * */
    @JvmOverloads
    fun diamond(
        pos: BlockPos,
        radius: Int,
        centerChance: Int = 100,
        chance: Int = 100
    ) = incDiamond(pos, centerChance, *Array(radius) { HorizontalLayer(chance) })

    /**
     * See docs for similar overload taking in a single BlockPos.
     *
     * @param positions an iterable of positions, placement starts at the first position, chances only proc once,
     *  then the shape is copied over to all other positions.
     *  Useful for making shapes extend themselves without running the chances again.
     * */
    @JvmOverloads
    fun diamond(
        positions: Iterable<BlockPos>,
        radius: Int,
        centerChance: Int = 100,
        chance: Int = 100
    ) = incDiamond(positions, centerChance, *Array(radius) { HorizontalLayer(chance) })

    /**
     * Makes a diamond with incremental chances for each layer.
     *
     * For example (Java):
     * ```
     * ctx.incDiamond(pos, 100, new HorizontalLayer(50), new HorizontalLayer(25));
     * ```
     * (Kotlin):
     * ```
     * ctx.incDiamond(pos, 100, HorizontalLayer(50), HorizontalLayer(25))
     * ```
     * Would result in:
     *  ```
     *  🆓 🆓 🏿 🆓 🆓 ️
     *  🆓 🏿 🏾 🏿 🆓
     *  🏿 🏾 🏽 🏾 🏿
     *  🆓 🏿 🏾 🏿 🆓
     *  🆓 🆓 🏿 🆓 🆓
     *  ```
     *  Where the chances of spawning a block are:
     *  🏽 - 100%; 🏾 - 50%; 🏿 - 25%;
     *
     *  @param pos the position from which placing starts. Usu. above/below trunk attachment position.
     *  @param centerChance chance specifically for the center block,
     *   separate from [layers] because all other [HorizontalLayer] params don't really apply to it,
     *   and you might want to alternate it quite often.
     *  @param layers an array of [HorizontalLayer], each defines settings for a single incremented outward layer,
     *   and the size of the array defines the radius of the resulting shape.
     *
     *  @see diamond
     *  @see incShape
     * */
    @JvmOverloads
    fun incDiamond(pos: BlockPos, centerChance: Int = 100, vararg layers: HorizontalLayer) =
        incShape(pos, centerChance, ShapeType.DIAMOND, *layers)

    /**
     * See docs for similar overload taking in a single BlockPos.
     *
     * @param positions an iterable of positions, placement starts at the first position, chances only proc once,
     *  then the shape is copied over to all other positions.
     *  Useful for making shapes extend themselves without running the chances again.
     * */
    @JvmOverloads
    fun incDiamond(positions: Iterable<BlockPos>, centerChance: Int = 100, vararg layers: HorizontalLayer) =
        incShape(positions, centerChance, ShapeType.DIAMOND, *layers)


    /**
     * Makes a disc of a certain radius at a certain position.
     *
     * @param radius radius.
     * @param pos the position from which placing starts. Usu. above/at/below trunk attachment position.
     * @param centerChance placement chance specifically for the center block, 1-100%.
     * @param chance chance of placing each individual block, 1-100%.
     *
     * See [incDisc] and [incShape] for more details
     *
     * @see incDisc
     * @see incShape
     * */
    @JvmOverloads
    fun disc(
        pos: BlockPos,
        radius: Int,
        centerChance: Int = 100,
        chance: Int = 100,
        smooth: Boolean = true
    ) = incDisc(pos, centerChance, smooth, *Array(radius) { HorizontalLayer(chance) })

    /**
     * See docs for similar overload taking in a single BlockPos.
     *
     * @param positions an iterable of positions, placement starts at the first position, chances only proc once,
     *  then the shape is copied over to all other positions.
     *  Useful for making shapes extend themselves without running the chances again.
     * */
    @JvmOverloads
    fun disc(
        positions: Iterable<BlockPos>,
        radius: Int,
        centerChance: Int = 100,
        chance: Int = 100,
        smooth: Boolean = true
    ) = incDisc(positions, centerChance, smooth, *Array(radius) { HorizontalLayer(chance) })

    /**
     * Makes a disc with incremental chances for each layer.
     *
     * For example (Java):
     * ```
     * ctx.incDisc(pos, true, 100, new HorizontalLayer(75), new HorizontalLayer(50), new HorizontalLayer(25));
     * ```
     * (Kotlin):
     * ```
     * ctx.incDisc(pos, true, 100, HorizontalLayer(75), HorizontalLayer(50), HorizontalLayer(25))
     * ```
     * Would result in:
     *  ```
     *  🆓 🆓 🏿 🏾 🏿 🆓 🆓 ️
     *  🆓 🏿 🏽 🏽 🏽 🏿 🆓
     *  🏿 🏽 🏼 🏼 🏼 🏽 🏿
     *  🏾 🏽 🏼 🏻 🏼 🏽 🏾
     *  🏿 🏽 🏼 🏼 🏼 🏽 🏿
     *  🆓 🏿 🏽 🏽 🏽 🏿 🆓
     *  🆓 🆓 🏿 🏾 🏿 🆓 🆓
     *  ```
     * Where the chances of spawning a block are:
     * 🏻 - 100%, 🏼 - 75%; 🏽 - 50%; 🏾 - 25%; 🏿 - 25%, blocks added by `smooth = true`
     *
     * @param pos the position from which placing starts. Usu. above/below trunk attachment position.
     * @param smooth removes the 1 block off the cardinal sides of the disc,
     *   makes it less like a star and more circular. Usually what you want.
     * @param centerChance chance specifically for the center block,
     *   separate from [layers] because all other [HorizontalLayer] params don't really apply to it,
     *   and you might want to alternate it quite often.
     * @param layers an array of [HorizontalLayer], each defines settings for a single incremented outward layer.
     *   The size of the array defines the radius of the resulting shape.
     *
     * @see disc
     * @see incShape
     * */
    @JvmOverloads
    fun incDisc(pos: BlockPos, centerChance: Int = 100, smooth: Boolean = true, vararg layers: HorizontalLayer) =
        incShape(pos, centerChance, ShapeType.DISC, *layers, discSmoothInternal = smooth)

    /**
     * See docs for similar overload taking in a single BlockPos.
     *
     * @param positions an iterable of positions, placement starts at the first position, chances only proc once,
     *  then the shape is copied over to all other positions.
     *  Useful for making shapes extend themselves without running the chances again.
     * */
    @JvmOverloads
    fun incDisc(
        positions: Iterable<BlockPos>,
        centerChance: Int = 100,
        smooth: Boolean = true,
        vararg layers: HorizontalLayer
    ) =
        incShape(positions, centerChance, ShapeType.DISC, *layers, discSmoothInternal = smooth)

}