package domain.entities

import com.example.domain.exceptions.CacheUsecaseException

class DoublyLinkedList(size: Int = 10) {
    private val tail: Node = Node.invoke(-1, "")
    private val current: Node = Node.invoke(-1, "")
    private var maxSize = size
    private var currentSize: Int = 0
    private val nodeMap: HashMap<Int, Node> = hashMapOf()

    init {
        current.setNext(tail)
        tail.setPrev(current)
    }

    companion object Factory {
        fun invoke(size: Int): DoublyLinkedList  {
            return DoublyLinkedList(size)
        }
    }

    fun setSize(newSize: Int) {
        if (newSize < currentSize)
            throw CacheUsecaseException.InvalidSizeException("New size is smaller than current size")
        maxSize = newSize
    }

    fun getSize(): Int   {
        return maxSize
     }

    fun isFull(): Boolean {
        return maxSize < currentSize
    }

    fun isEmpty(): Boolean  {
        return currentSize == 0
    }

    fun containsNode(key: Int): Boolean {
        return nodeMap[key] != null
    }

    fun getNode(key: Int): Node {
        return nodeMap[key]!!
    }

    private fun insertNodeIn(node: Node, left: Node, right: Node) {
        if (isFull())
            throw CacheUsecaseException.FullCacheException("Cache is full")

        left.setNext(node)
        node.setPrev(left)

        node.setNext(right)
        right.setPrev(node)

        nodeMap[node.getKey()] = node
        currentSize++
    }

    fun insertNodeAtHead(node: Node) {
        insertNodeIn(node, current, current.getNext()!!)
    }

    fun deleteLastNode() {
        if (isEmpty())
            throw CacheUsecaseException.EmptyCacheException("Cache is empty")

        deleteNode(tail.getPrev()!!)
    }

    fun deleteNode(node: Node) {
        val prev: Node = node.getPrev()!!
        val next: Node = node.getNext()!!

        prev.setNext(next)
        next.setPrev(prev)

        nodeMap.remove(node.getKey())
        currentSize--
    }

    fun getNodes(): List<Node> {
        mutableListOf<Node>().let { list ->
            var node: Node = current.getNext()!!
            list.add(node)

            while (node != tail) {
                node = node.getNext()!!
                list.add(node)
            }

            return list
        }
    }
}
