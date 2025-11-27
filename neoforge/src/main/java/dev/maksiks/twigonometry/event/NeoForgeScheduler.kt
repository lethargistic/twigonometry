package dev.maksiks.twigonometry.event

import dev.maksiks.twigonometry.Constants
import dev.maksiks.twigonometry.api.LeafPlacerContext
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.LevelSimulatedReader
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.tick.ServerTickEvent
import java.util.concurrent.ConcurrentLinkedQueue

private data class ScheduledTask(
    val delayTicks: Int,
    val run: () -> Boolean,
    var tickCounter: Int = 0
)

/**
 * In worldgen anything here is executed immediately, this works great for saplings tho.
 */
@EventBusSubscriber(modid = Constants.MOD_ID)
object NeoForgeScheduler : LeafPlacerContext.TwigScheduler.IScheduler {

    private val tasks = mutableListOf<ScheduledTask>()
    private val pendingTasks = ConcurrentLinkedQueue<ScheduledTask>()

    override fun scheduleRepeating(level: LevelSimulatedReader, delayTicks: Int, task: () -> Boolean) {
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

    @SubscribeEvent
    fun onServerTick(e: ServerTickEvent.Post) {
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