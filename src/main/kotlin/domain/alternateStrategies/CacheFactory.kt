package com.example.domain.alternateStrategies

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object CacheFactory {

    @Volatile
    private var lfuInstance: LFUCache<Any?, Any?>? = null
    private val lruInstances: ConcurrentMap<Int, LRUCache<Any?, Any?>> = ConcurrentHashMap()

    /**
     * Returns the shared [LFUCache] instance, creating it on the first call.
     * [capacity] is only honored on the first call; later calls ignore it.
     * All callers must use the same [K]/[V] types — the instance is shared,
     * so mixing types across call sites is unsafe.
     */
    @Suppress("UNCHECKED_CAST")
    fun <K, V> getLfuCache(capacity: Int = 20): LFUCache<K, V> {
        val existing = lfuInstance // ① fast path — no lock

        if (existing != null) 
            return existing as LFUCache<K, V>

        return synchronized(this) { // ② slow path — one thread at a time
            lfuInstance ?: LFUCache<Any?, Any?>(capacity).also { lfuInstance = it } 
        } as LFUCache<K, V>
    }

    /** Returns a new independent [LRUCache] on every call. */
    @Suppress("UNCHECKED_CAST")
    fun <K, V> getLruCache(userId: Int, capacity: Int = 5): LRUCache<K, V> =
        lruInstances.computeIfAbsent(userId) { LRUCache(capacity) } as LRUCache<K, V>
}

/*

Every object on the JVM has a built-in lock (called a monitor). synchronized(this) { ... } means: before entering this block, 
the current thread must acquire the lock on this (our CacheFactory object). Only one thread can hold that lock at a time. 
Anyone else who reaches the block while it's held simply waits at the entrance until the holder exits the block and releases the lock.

So a synchronized block turns "many threads may run this code simultaneously" into "threads run this code strictly one at a time." 

Replaying the same race:

1. Threads A and B both read lfuInstance at ① and see null.
2. Both head for the synchronized block. A gets the lock; B waits at the door.
3. Inside, A checks lfuInstance ?: — still null — so A creates the cache, stores it, and leaves the block, releasing the lock.
4. Now B enters. This is the crucial moment: B checks lfuInstance ?: again, and this time it's not null — B returns A's instance instead of creating a second one.


It also has a second, less obvious job: when a thread releases the lock, everything it wrote to memory becomes visible to the next thread that acquires it.

Where @Volatile fits in
One subtlety: the fast path at ① reads lfuInstance without the lock, and the memory-visibility guarantee I mentioned only applies between threads that both 
use the lock. @Volatile covers the gap — it guarantees that when A publishes the instance inside the block, a thread reading at ① (never having touched the lock) 
still sees the fully constructed object, not null or a half-initialized reference. The pattern needs both pieces: synchronized prevents double creation, 
@Volatile makes the lock-free read safe. Drop either one and double-checked locking is broken.

What actually happens in case of coroutines?
A coroutine always executes on some thread (whichever one its dispatcher assigned). The monitor lock from @Synchronized 
operates at the thread level — it neither knows nor cares that coroutines exist. So:

Coroutine A, running on thread T1, calls cache.put(...) and takes the lock.
Coroutine B, running on thread T2, calls cache.get(...) at the same moment.
Thread T2 blocks at the method entrance until T1 releases the lock.
So coroutine B does wait — mutual exclusion works correctly for coroutines. But B waits by blocking its carrier thread, not by suspending. That distinction is the whole story:

Suspending (what delay() or Mutex.withLock does): the coroutine parks, its thread is released back to the dispatcher and immediately runs other coroutines. Waiting is free.
Blocking (what synchronized does): the thread itself stops, and everything else scheduled on that thread waits too. The dispatcher has one fewer worker until the lock opens.
Why it's still the right call for our cache
Blocking is only a problem in proportion to how long the lock is held. LFUCache.get/put do a handful of in-memory HashMap operations — the lock is held for microseconds. 
A thread blocking for microseconds is cheaper than a coroutine suspension (which involves scheduling machinery of its own). This is the accepted rule: short, CPU-only, 
non-suspending critical sections are fine under synchronized, coroutines or not.

The alternative would be the coroutine-native lock:


private val mutex = Mutex()

suspend fun get(key: K): V? = mutex.withLock { ... }
Note what that costs us: get and put must become suspend functions, which forces every caller up the chain into a coroutine context. Your cache could no longer be called 
from plain non-suspending code. For a microsecond critical section, that's a bad trade — so I'd keep @Synchronized here.

When synchronized + coroutines does go wrong
Two situations to keep in your head for ZipReel:

Long work under the lock. If a locked section did disk or network I/O, every coroutine that touches the cache would blockade its thread for the full duration. 
On Dispatchers.Default (thread count = CPU cores, so often just 4–8), a few blocked threads can stall unrelated coroutines that merely wanted a turn on the pool. 
That's how "the cache got slow" turns into "the whole app got janky."

Suspending while holding the lock — never do this. Monitor locks are owned by a thread. If code inside a synchronized block hit a suspension point, the coroutine could 
resume on a different thread that doesn't own the monitor — the lock/unlock pairing breaks, and you can also deadlock the dispatcher (all pool threads blocked on a monitor 
whose releasing coroutine can't get a thread to resume on). Our methods can't hit this since nothing in them suspends, but it's the reason the rule exists: the moment a 
critical section needs to suspend, switch to Mutex.withLock — it's suspension-safe because it tracks the coroutine as the owner, not the thread.

Where do ConcurrentHashMap come in?

A plain HashMap is unsafe under concurrent writes — two threads resizing or linking entries at once can corrupt the internal structure (lost entries, 
even infinite loops in old JVM versions). The blunt fix is wrapping every call in one lock, like our @Synchronized, but then all threads queue behind a 
single monitor even when touching completely unrelated keys.

ConcurrentHashMap solves this with fine-grained locking: instead of one lock over the whole table, it locks at the level of an individual hash bucket. 
Writes to different buckets proceed fully in parallel — a thread writing key "batman" doesn't wait for a thread writing key "inception" at all. Even better, 
reads take no lock whatsoever: get runs on volatile reads of the table, so any number of readers proceed concurrently even while writers are active.

It also gives you atomic compound operations that a plain map can't:

map.computeIfAbsent(key) { expensiveLoad(key) }  // check + insert as ONE atomic step
map.putIfAbsent(key, value)
map.compute(key) { _, old -> (old ?: 0) + 1 }    // atomic read-modify-write
With a plain HashMap, "check if absent, then insert" is two operations with a race window between them — the same check-then-act race we walked through in CacheFactory. 
ConcurrentHashMap performs those as one indivisible step, per key, with no external lock.

So relative to @Synchronized-around-everything, it's a throughput upgrade: readers never wait, and writers only contend when they collide on the same bucket.
*/
