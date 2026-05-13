package registry

import domain.entities.DoublyLinkedList
import domain.stategies.LruCacheEviction
import domain.usecases.CacheUsecase

object CacheUsecaseRegistry {

    private val cacheUsecase: CacheUsecase by lazy {
        CacheUsecase(LruCacheEviction.invoke(10, DoublyLinkedList.invoke(10)))
    }

    fun getInstance(): CacheUsecase {
        return cacheUsecase
    }
}