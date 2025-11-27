package dev.maksiks.twigonometry.event

import dev.maksiks.twigonometry.Constants
import dev.maksiks.twigonometry.api.LeafPlacerContext
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.LevelSimulatedReader
import java.util.concurrent.ConcurrentLinkedQueue

private data class ScheduledTask(
    val delayTicks: Int,
    val run: () -> Boolean,
    var tickCounter: Int = 0
)

/**
 * In worldgen anything here is executed immediately, this works great for saplings tho.
 */
object FabricScheduler : LeafPlacerContext.TwigScheduler.IScheduler {

    private val tasks = mutableListOf<ScheduledTask>()
    private val pendingTasks = ConcurrentLinkedQueue<ScheduledTask>()

    fun register() {
        Constants.LOG.info("FabricScheduler.register() called!")
        LeafPlacerContext.TwigScheduler.init(this)
        Constants.LOG.info("TwigScheduler.init() completed!")

        // Register the tick event
        ServerTickEvents.END_SERVER_TICK.register { server ->
            while (true) {
                val pending = pendingTasks.poll() ?: break
                tasks.add(pending)
            }

            tasks.removeIf { task ->
                task.tickCounter++
                if (task.tickCounter >= task.delayTicks) {
                    task.tickCounter = 0
                    val shouldContinue = try {
                        task.run()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                    !shouldContinue
                } else {
                    false
                }
            }
        }
    }

    override fun scheduleRepeating(level: LevelSimulatedReader, delayTicks: Int, task: () -> Boolean) {
        Constants.LOG.info("HAI KORE")
        val scheduledTask = ScheduledTask(delayTicks, task)

        if (level is ServerLevel && level.server != null) {
            pendingTasks.offer(scheduledTask)
        } else {
            var shouldContinue = true
            while (shouldContinue) {
                try {
                    shouldContinue = task()
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }
}