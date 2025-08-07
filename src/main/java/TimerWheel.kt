import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport

class TimerWheel(private val wheelSize: Int = 60) {

    private var startTime: Long = 0L
    private var wheel: Array<Slot> = Array(wheelSize) { Slot() }
    private var started: AtomicBoolean = AtomicBoolean(false)
    private val ticker: Ticker = Ticker()
    private val newTasks = ConcurrentLinkedQueue<DelayTask>()

    class Slot {
        val tasks = ConcurrentLinkedQueue<DelayTask>()

        fun runWithDeadline(tickTime: Long) {
            val iterator = tasks.iterator()
            while (iterator.hasNext()) {
                val task = iterator.next()
                if (task.round > 0) {
                    task.round--
                } else {
                    if (task.deadline <= tickTime) {
                        task.runnable.run()
                        iterator.remove()
                    }
                }
            }
        }
    }

    fun start() {
        if (started.compareAndSet(false, true)) {
            ticker.start()
        }
    }

    fun stop() {
        if (started.compareAndSet(true, false)) {
            ticker.interrupt()
        }
    }

    fun addDelayTask(runnable: Runnable, delayMs: Long) {
        val deadline = System.currentTimeMillis() + delayMs
        newTasks.add(DelayTask(runnable, deadline, 0)) // round will be calculated in ticker thread
    }

    data class DelayTask(
        val runnable: Runnable,
        val deadline: Long,
        var round: Int
    )

    inner class Ticker : Thread() {
        private var tickCount = 0
        override fun run() {
            startTime = System.currentTimeMillis()
            while (started.get()) {
                while(newTasks.isNotEmpty()) {
                    val task = newTasks.poll()
                    if (task != null) {
                        val delayMs = task.deadline - System.currentTimeMillis()
                        if (delayMs <= 0) {
                            // execute immediately
                            task.runnable.run()
                            continue
                        }
                        val totalTicks = (delayMs / 100).toInt()
                        task.round = totalTicks / wheelSize
                        val slotIndex = (tickCount + totalTicks) % wheelSize
                        wheel[slotIndex].tasks.add(task)
                    }
                }

                val tickTime = startTime + ((tickCount + 1) * 100L)
                LockSupport.parkUntil(tickTime)

                val index = tickCount % wheel.size
                val slot = wheel[index]
                slot.runWithDeadline(tickTime)
                tickCount++
            }
        }
    }
}