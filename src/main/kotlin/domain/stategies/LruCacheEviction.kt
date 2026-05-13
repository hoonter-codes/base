package domain.stategies

import domain.entities.DoublyLinkedList
import domain.entities.Node

class LruCacheEviction(
    maxSize: Int,
    doublyLinkedList: DoublyLinkedList
) : CacheEvictionStrategy{

    val dll: DoublyLinkedList = doublyLinkedList

    init {
        dll.setSize(maxSize)
    }

    companion object Factory {
        fun invoke(maxSize: Int, doublyLinkedList: DoublyLinkedList): LruCacheEviction {
            return LruCacheEviction(
                maxSize = maxSize,
                doublyLinkedList = doublyLinkedList
            )
        }
    }

    override fun put(key: Int, value: String) {
        println("using the LRU strategy")
        if (dll.containsNode(key)) {
            val node = dll.getNode(key)
            dll.deleteNode(node)
            dll.insertNodeAtHead(node)
            node.setValue(value)
        } else {
            val node = Node.invoke(key, value)
            dll.insertNodeAtHead(node)
            if (dll.isFull())
                dll.deleteLastNode()
        }
    }

    override fun get(key: Int): String? {
        println("using the LRU strategy")
        return if (dll.containsNode(key)) {
            val node = dll.getNode(key)
            dll.deleteNode(node)
            dll.insertNodeAtHead(node)
            node.getValue()
        } else null
    }

    override fun setSize(size: Int) {
        println("using the LRU strategy")
        dll.setSize(size)
    }

    override fun switch(): CacheEvictionStrategy {
        println("using the LRU strategy, changing to LFU strategy")
        return LfuCacheEviction.invoke(
            maxSize = dll.getSize(),
            doublyLinkedList = dll
        )
    }

}
