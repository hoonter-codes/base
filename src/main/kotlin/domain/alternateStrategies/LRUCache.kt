package com.example.domain.alternateStrategies

class LRUCache<K, V>(
    private val capacity: Int
) : LinkedHashMap<K, V>(capacity, 0.75f, true) {

    // accessOrder = true means even get() mutates the internal access list,
    // so reads need the lock just like writes.
    @Synchronized
    override fun get(key: K): V? = super.get(key)

    @Synchronized
    override fun put(key: K, value: V): V? = super.put(key, value)

    @Synchronized
    override fun putAll(from: Map<out K, V>) = super.putAll(from)

    @Synchronized
    override fun remove(key: K): V? = super.remove(key)

    @Synchronized
    override fun clear() = super.clear()

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > capacity
    }
}
