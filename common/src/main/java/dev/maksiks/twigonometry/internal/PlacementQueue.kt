package dev.maksiks.twigonometry.internal

import dev.maksiks.twigonometry.Constants
import dev.maksiks.twigonometry.api.LeafPlacerContext.TwigScheduler
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.RandomSource
import net.minecraft.world.RandomizableContainer
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.levelgen.feature.TreeFeature
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import org.apache.logging.log4j.util.BiConsumer
import java.util.function.Predicate

@Internal
interface BlockSetterWrapper {
    fun set(pos: BlockPos, state: BlockState)

    @JvmInline
    value class Foliage(val setter: FoliagePlacer.FoliageSetter) : BlockSetterWrapper {
        override fun set(pos: BlockPos, state: BlockState) = setter.set(pos, state)
    }

    // why did they not make a separate type for it in vanilla? Mojank
    @JvmInline
    value class Trunk(val setter: BiConsumer<BlockPos, BlockState>) : BlockSetterWrapper {
        override fun set(pos: BlockPos, state: BlockState) = setter.accept(pos, state)
    }
}

@Internal
class PlacementQueue(
    val step: Integer?,
    val blockSetter: BlockSetterWrapper,
    val level: WorldGenLevel,
    val random: RandomSource,
) {

    // TODO twig cur fix: resolve nulll
    private data class PlacementOperation(
        val pos: BlockPos,
        var state: BlockState,
        val flags: Int = 19,
        val nbt: CompoundTag? = null
    )

    // TODO twig cur: nice error net if q has stuff but step is 0
    private val queue = mutableListOf<PlacementOperation>()

    fun isStepByStep(): Boolean {
        return step != null && step > 0
    }

    // TODO: think whether to switch to setblock entirely rather than the blocksetter
    /**
     * @flags vanilla setblock flags, if provided uses setblock rather than the blocksetter
     */
    fun enqueue(pos: BlockPos, state: BlockState, flags: Int = 19, nbt: CompoundTag? = null) {
        queue.add(PlacementOperation(pos, state, flags, nbt))
    }

    fun enqueueCarve(pos: BlockPos) {
        queue.add(PlacementOperation(pos, Blocks.AIR.defaultBlockState()))
    }

    private fun scheduleRepeatingTask(level: WorldGenLevel, delayTicks: Int, task: () -> Boolean) {
        TwigScheduler.scheduleRepeating(level, delayTicks, task)
    }

    fun processQueueInternal(level: WorldGenLevel, blocksPerStep: Int = 1, onComplete: (() -> Unit)? = null) {
        if (!isStepByStep() || queue.isEmpty()) {

            onComplete?.invoke()
            return
        }

        val q = queue.toMutableList()
        queue.clear()
        val delayMs = step!!
        val delayTicks = (delayMs.toDouble() / 50.0).coerceAtLeast(1.0).toInt()

        var currentIndex = 0

        fun processSetOfBlocks(): Boolean {
            val endIndex = minOf(currentIndex + blocksPerStep, q.size)

            for (i in currentIndex until endIndex) {
                val operation = q[i]

                // TODO twig cur: remake this to be explicit
                // TODO twig cur: make it work without q
                // TODO twig cur fig: consider adding age to q only after fully placing to prevent them disappearing
                // 19 is what vanilla uses for the setters
                if (operation.flags == 19) {
                    // TODO twig cur fig: tryPlaceLeaf, imo copying it over here is less ugly than passing in the leaf placer but mayb
                    if (!TreeFeature.validTreePos(level, operation.pos)) {
                        continue
                    } else {
                        if (operation.state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                            operation.state = operation.state.setValue(
                                BlockStateProperties.WATERLOGGED,
                                level.isFluidAtPosition(
                                    operation.pos
                                ) { p_225638_: FluidState? -> p_225638_!!.isSourceOfType(Fluids.WATER) }
                            ) as BlockState
                        }

                        blockSetter.set(operation.pos, operation.state)
                    }
                    continue
                }
                if (level.setBlock(operation.pos, operation.state, operation.flags)) {
                    Constants.LOG.info("Twigonoe: placing: $operation.pos")
                    if (operation.nbt != null) {
                        val blockEntity = level.getBlockEntity(operation.pos)

                        if (blockEntity != null) {
                            if (blockEntity is RandomizableContainer) {
                                operation.nbt.putLong("LootTableSeed", random.nextLong())
                            }

                            blockEntity.loadWithComponents(operation.nbt, level.registryAccess())
                        }
                    }
                }

            }

            currentIndex = endIndex
            return currentIndex >= q.size
        }

        scheduleRepeatingTask(level, delayTicks) {
            val isDone = processSetOfBlocks()
            if (isDone) {
                onComplete?.invoke()
                false
            } else {
                true
            }
        }
    }
}