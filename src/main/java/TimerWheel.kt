import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport

class TimerWheel {

    private var startTime: Long = 0L
    private var wheel: Array<Slot> = arrayOf()
    private var started: AtomicBoolean = AtomicBoolean(false)

    class Slot {
        fun runWithDeadLine(tickTime: Long): Unit {

        }
    }

    private fun start() = started.compareAndSet(false, true)
    private fun stop() = started.compareAndSet(true, false)

    fun addDelayTask(runnable: Runnable, delayMs: Long) {

    }

    class DelayTask(
        val runnable: Runnable,
        val delay: Long,
        val next: DelayTask
    ) {
        val deadline = System.currentTimeMillis() + delay
    }

    inner class Ticker : Thread() {
        var tickCount = 0
        override fun run() {
            startTime = System.currentTimeMillis()
            while (true) {
                var tickTime = startTime + ((tickCount + 1) * 100L)
                while (tickTime >= System.currentTimeMillis()) {
                    LockSupport.parkUntil(tickTime)
                }
                var index = tickCount % wheel.size
                var slot = wheel[index]
                slot.runWithDeadLine(tickTime)
                tickCount++
            }
        }
    }

}