package com.saeed.practice;

import java.util.LinkedHashMap;
import java.util.Optional;

public class LruCache<K, V> {

    private final int capacity;
    private final LinkedHashMap<K, V> cache;

    public LruCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = capacity;
        cache = new LinkedHashMap<>(capacity, 0.75f, false);
    }

    public synchronized Optional<V> get(K key) {
        V val = cache.remove(key);
        if (val == null) {
            return Optional.empty();
        }
        cache.putLast(key, val);
        return Optional.of(val);
    }

    public synchronized void add(K key, V val) {
        if (cache.size() >= capacity && !cache.containsKey(key)) {
            cache.pollFirstEntry();
        }
        cache.putLast(key, val);
    }

    public synchronized int size() {
        return cache.size();
    }

    public synchronized void clear() {
        cache.clear();
    }

    public synchronized boolean containsKey(K key) {
        return cache.containsKey(key);
    }
}
