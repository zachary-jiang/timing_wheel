import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

class TimerWheelTest {

    @Test
    fun `test single task`() {
        val timerWheel = TimerWheel()
        timerWheel.start()
        val latch = CountDownLatch(1)
        val startTime = System.currentTimeMillis()
        var executionTime = 0L

        timerWheel.addDelayTask(Runnable {
            executionTime = System.currentTimeMillis()
            latch.countDown()
        }, 1000)

        latch.await(2000, TimeUnit.MILLISECONDS)
        timerWheel.stop()

        val delay = executionTime - startTime
        assertTrue(delay in 900..1100, "Task was not executed within the expected time frame.")
    }

    @Test
    fun `test multiple tasks`() {
        val timerWheel = TimerWheel()
        timerWheel.start()
        val latch = CountDownLatch(2)
        val executionTimes = LongArray(2)
        val startTime = System.currentTimeMillis()

        timerWheel.addDelayTask(Runnable {
            executionTimes[0] = System.currentTimeMillis()
            latch.countDown()
        }, 500)

        timerWheel.addDelayTask(Runnable {
            executionTimes[1] = System.currentTimeMillis()
            latch.countDown()
        }, 1000)

        latch.await(3000, TimeUnit.MILLISECONDS)
        timerWheel.stop()

        val delay1 = executionTimes[0] - startTime
        val delay2 = executionTimes[1] - startTime

        assertTrue(delay1 in 400..600, "Task 1 was not executed within the expected time frame.")
        assertTrue(delay2 in 900..1100, "Task 2 was not executed within the expected time frame.")
    }

    @Test
    fun `test stop timer wheel`() {
        val timerWheel = TimerWheel()
        timerWheel.start()
        val latch = CountDownLatch(1)

        timerWheel.addDelayTask(Runnable {
            latch.countDown()
        }, 2000)

        timerWheel.stop()
        val executed = latch.await(3000, TimeUnit.MILLISECONDS)

        assertTrue(!executed, "Task should not have been executed after stopping the timer wheel.")
    }
}
