package com.example.domain.alternateStrategies


class LRUCache<K, V>(
    private val capacity: Int
) {

    // Completely encapsulated inside the class
    private val internalMap = object : LinkedHashMap<K, V>(capacity, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return this.size > capacity
        }
    }

    // accessOrder = true means even get() mutates the internal access list as it should in a LRU cache,
    // so reads need the lock just like writes.
    @Synchronized
    fun get(key: K): V? = internalMap[key]

    @Synchronized
    fun put(key: K, value: V) {
        internalMap[key] = value
    }

    @Synchronized
    fun remove(key: K): V? = internalMap.remove(key)

    @Synchronized
    fun clear() = internalMap.clear()

    // Ensure state inspectors are also synchronized!
    val size: Int
        @Synchronized get() = internalMap.size
}
