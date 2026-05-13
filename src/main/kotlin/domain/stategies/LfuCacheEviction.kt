package domain.stategies

import com.example.domain.exceptions.CacheUsecaseException
import domain.entities.DoublyLinkedList
import domain.entities.Node

class LfuCacheEviction(
    var maxSize: Int,
    doublyLinkedList: DoublyLinkedList
) : CacheEvictionStrategy {

    val frequencyDllMap = hashMapOf(1 to doublyLinkedList)
    val keyFrequencyMap = hashMapOf<Int, Int>()

    init {
        doublyLinkedList.getNodes().forEach { node ->
            keyFrequencyMap[node.getKey()] = 1
        }
    }

    companion object Factory {
        fun invoke(maxSize: Int, doublyLinkedList: DoublyLinkedList): LfuCacheEviction {
            return LfuCacheEviction(
                maxSize = maxSize,
                doublyLinkedList = doublyLinkedList
            )
        }
    }

    override fun put(key: Int, value: String) {
        println("using the LFU strategy")
        if (keyFrequencyMap.containsKey(key)) {
            val frequency = keyFrequencyMap[key]!!
            val prevDll = frequencyDllMap[frequency]!!
            val node = prevDll.getNode(key)

            prevDll.deleteNode(node)
            keyFrequencyMap.remove(key)
            node.setValue(value)

            keyFrequencyMap[key] = frequency + 1
            val nextDll = frequencyDllMap[frequency + 1] ?: DoublyLinkedList.invoke(maxSize)
            nextDll.insertNodeAtHead(node)
        } else {
            if (keyFrequencyMap.size == maxSize)
                throw CacheUsecaseException.FullCacheException("Cache is full")

            val node = Node.invoke(key, value)
            keyFrequencyMap[key] = 1
            val dll = frequencyDllMap[1]!!
            dll.insertNodeAtHead(node)
        }
    }

    override fun get(key: Int): String? {
        println("using the LFU strategy")
        return if (keyFrequencyMap.containsKey(key)) {
            val frequency = keyFrequencyMap[key]!!
            val prevDll = frequencyDllMap[frequency]!!
            val node = prevDll.getNode(key)

            prevDll.deleteNode(node)
            keyFrequencyMap.remove(key)
            keyFrequencyMap[key] = frequency + 1
            val nextDll = frequencyDllMap[frequency + 1] ?: DoublyLinkedList.invoke(maxSize)
            nextDll.insertNodeAtHead(node)
            node.getValue()
        } else null
    }

    override fun setSize(size: Int) {
        println("using the LFU strategy")
        this.maxSize = size

        for (dll in frequencyDllMap.values)
            dll.setSize(size)
    }

    override fun switch(): CacheEvictionStrategy {
        println("using the LFU strategy, changing to LRU strategy")
        val maxFrequency = keyFrequencyMap.maxBy { it.value }.key
        val dll = DoublyLinkedList.invoke(maxSize)

        for (i in 1..maxFrequency) {
            val tempDll = frequencyDllMap[i] ?: continue
            val nodes = tempDll.getNodes().reversed()

            for (node in nodes)
                dll.insertNodeAtHead(node)
        }

        return LruCacheEviction.invoke(
            maxSize = maxSize,
            doublyLinkedList = dll
        )
    }


}