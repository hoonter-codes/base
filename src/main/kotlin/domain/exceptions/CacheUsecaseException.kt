package com.example.domain.exceptions

sealed class CacheUsecaseException(override val message: String) : Exception() {

    class FullCacheException(override val message: String) : CacheUsecaseException(message)

    class InvalidSizeException(override val message: String) : CacheUsecaseException(message)

    class EmptyCacheException(override val message: String) : CacheUsecaseException(message)
}
