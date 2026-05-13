package domain.stategies

interface CacheEvictionStrategy {
    fun put(key: Int, value: String)
    fun get(key: Int): String?
    fun setSize(size: Int)
    fun switch(): CacheEvictionStrategy
}