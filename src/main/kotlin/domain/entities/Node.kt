package domain.entities

class Node(
    private val key: Int,
    private var value: String,
    private var prev: Node? = null,
    private var next: Node? = null
) {
    fun getKey(): Int {
        return key
    }

    fun getValue(): String {
        return value
    }

    fun setValue(value: String) {
        this.value = value
    }

    fun getPrev(): Node? {
        return prev
    }

    fun setPrev(prev: Node?) {
        this.prev = prev
    }

    fun getNext(): Node? {
        return next
    }

    fun setNext(next: Node?) {
        this.next = next
    }

    companion object Factory {
        fun invoke(key: Int, value: String): Node {
            return Node(key, value)
        }
    }
}

