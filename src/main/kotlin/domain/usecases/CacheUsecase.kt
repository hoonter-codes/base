package domain.usecases

import domain.stategies.CacheEvictionStrategy

class CacheUsecase(
    cacheEviction: CacheEvictionStrategy
) {
    var currentStrategy = cacheEviction

    fun put(key: Int, value: String) {
        currentStrategy.put(key, value)
    }

    fun get(key: Int): String? {
        return currentStrategy.get(key)
    }

    fun setSize(size: Int) {
        currentStrategy.setSize(size)
    }

    fun switchStrategy() {
        currentStrategy = currentStrategy.switch()
    }
}