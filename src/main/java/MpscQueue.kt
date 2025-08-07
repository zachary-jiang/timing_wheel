import java.util.concurrent.atomic.AtomicReference

/**
 * A lock-free Multiple-Producer Single-Consumer (MPSC) queue.
 *
 * This implementation is based on the well-known Michael-Scott queue algorithm,
 * but simplified for the single-consumer case.
 */
@Suppress("UNCHECKED_CAST")
class MpscQueue<T> {

    private class Node<T>(val value: T) {
        val next = AtomicReference<Node<T>?>()
    }

    private val head: AtomicReference<Node<T>>
    private var tail: Node<T>

    init {
        // Start with a dummy node to simplify the logic
        val dummy = Node(null as T) // The value is irrelevant
        head = AtomicReference(dummy)
        tail = dummy
    }

    /**
     * Adds a new value to the tail of the queue.
     * This method is thread-safe and can be called by multiple producers.
     */
    fun offer(value: T) {
        val newNode = Node(value)
        val prevHead = head.getAndSet(newNode)
        prevHead.next.set(newNode)
    }

    /**
     * Retrieves and removes the head of the queue.
     * This method is NOT thread-safe and must only be called by a single consumer.
     */
    fun poll(): T? {
        val nextNode = tail.next.get()
        if (nextNode != null) {
            tail = nextNode
            return nextNode.value
        }
        return null
    }

    /**
     * Checks if the queue is empty.
     * This method should ideally be called by the consumer thread for accuracy.
     */
    fun isEmpty(): Boolean {
        return tail.next.get() == null
    }
}
