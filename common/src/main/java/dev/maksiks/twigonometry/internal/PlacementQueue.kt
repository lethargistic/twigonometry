package dev.maksiks.twigonometry.internal

import dev.maksiks.twigonometry.api.LeafPlacerContext.TwigScheduler
import net.minecraft.core.BlockPos
import net.minecraft.world.level.LevelSimulatedReader
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import org.apache.logging.log4j.util.BiConsumer

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
    val blockSetter: BlockSetterWrapper,
    val step: Integer?
) {

    // TODO twig cur fix: resolve nulll
    private data class PlacementOperation(
        val pos: BlockPos,
        val state: BlockState
    )

    private val queue = mutableListOf<PlacementOperation>()

    fun isStepByStep(): Boolean {
        return step != null && step > 0
    }

    fun enqueue(pos: BlockPos, state: BlockState) {
        queue.add(PlacementOperation(pos, state))
    }

    fun enqueueCarve(pos: BlockPos) {
        queue.add(PlacementOperation(pos, Blocks.AIR.defaultBlockState()))
    }

    fun placeOrEnqueue(pos: BlockPos, state: BlockState) {
        if (isStepByStep()) enqueue(pos, state)
        else blockSetter.set(pos, state)
    }

    private fun scheduleRepeatingTask(level: LevelSimulatedReader, delayTicks: Int, task: () -> Boolean) {
        TwigScheduler.scheduleRepeating(level, delayTicks, task)
    }

    fun processQueueInternal(level: LevelSimulatedReader, blocksPerStep: Int = 1, onComplete: (() -> Unit)? = null) {
        if (!isStepByStep() || queue.isEmpty()) {
            onComplete?.invoke()
            return
        }

        val q = queue
        queue.clear()
        val delayMs = step!!
        val delayTicks = (delayMs.toDouble() / 50.0).coerceAtLeast(1.0).toInt()

        var currentIndex = 0

        fun processSetOfBlocks(): Boolean {
            val endIndex = minOf(currentIndex + blocksPerStep, q.size)

            for (i in currentIndex until endIndex) {
                val operation = q[i]

                blockSetter.set(operation.pos, operation.state)
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