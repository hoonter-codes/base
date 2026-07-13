package com.example.domain.alternateStrategies

import java.util.LinkedHashSet

class LFUCache<K, V>(private val capacity: Int) {

    private data class Node<K, V>(
        val key: K,
        var value: V,
        var freq: Int = 1
    )

    private val keyNodeMap = HashMap<K, Node<K, V>>()
    private val freqKeysMap = HashMap<Int, LinkedHashSet<K>>()
    private var minFreq = 0

    /*
        @Synchronized
        fun put(key: K, value: V) { }

        compiles to the same thing as:

        fun put(key: K, value: V) { synchronized(this) { } }
    */

    @Synchronized
    fun get(key: K): V? {
        val node = keyNodeMap[key] ?: return null
        updateFrequency(node)
        return node.value
    }

    @Synchronized
    fun put(key: K, value: V) {
        if (capacity <= 0) return

        if (keyNodeMap.containsKey(key)) {
            val node = keyNodeMap[key]!!
            node.value = value
            updateFrequency(node)
            return
        }

        if (keyNodeMap.size >= capacity) {
            val lfuKeyList = freqKeysMap[minFreq]
            val evictKey = lfuKeyList?.iterator()?.next()
            if (evictKey != null) {
                lfuKeyList.remove(evictKey)
                if (lfuKeyList.isEmpty()) {
                    freqKeysMap.remove(minFreq)
                }
                keyNodeMap.remove(evictKey)
            }
        }

        val newNode = Node(key, value)
        keyNodeMap[key] = newNode
        freqKeysMap.computeIfAbsent(1) { LinkedHashSet() }.add(key)
        minFreq = 1
    }

    private fun updateFrequency(node: Node<K, V>) {
        val currentFreq = node.freq
        val keysAtCurrentFreq = freqKeysMap[currentFreq]

        keysAtCurrentFreq?.remove(node.key)
        if (keysAtCurrentFreq.isNullOrEmpty()) {
            freqKeysMap.remove(currentFreq)
            if (minFreq == currentFreq) {
                minFreq++
            }
        }

        node.freq++
        freqKeysMap.computeIfAbsent(node.freq) { LinkedHashSet() }.add(node.key)
    }
}
