package com.saeed.practice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LruCacheTest {

    int capacity = 2;
    LruCache<String, String> cache;

    @Test
    void testIllegalCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new LruCache<>(0));
    }

    @BeforeEach
    void prepareCache() {
        cache = new LruCache<>(capacity);
    }

    @Test
    void testAddedValuesExistence() {
        for (int i = 0; i < 4; i++) {
            cache.add("key" + i, "val" + i);
        }
        assertFalse(cache.get("key0").isPresent());
        assertFalse(cache.get("key1").isPresent());
        assertEquals("val2", cache.get("key2").get());
        assertEquals("val3", cache.get("key3").get());
    }

    @Test
    void testLruEvictionAfterAccess() {
        cache.add("key0", "val0");
        cache.add("key1", "val1");
        cache.get("key0");
        cache.add("key2", "val2");

        assertFalse(cache.get("key1").isPresent());
        assertEquals("val0", cache.get("key0").get());
        assertEquals("val2", cache.get("key2").get());
    }

    @Test
    void testLruPromotionOnReinsertion() {
        cache.add("key0", "val0");
        cache.add("key1", "val1");
        cache.add("key0", "val0");
        cache.add("key2", "val2");

        assertFalse(cache.get("key1").isPresent());
        assertEquals("val0", cache.get("key0").get());
        assertEquals("val2", cache.get("key2").get());
    }

    @Test
    void testCacheSizeWithInsertionsAndEvictions() {
        assertEquals(0, cache.size());
        cache.add("key0", "val0");
        assertEquals(1, cache.size());
        cache.add("key0", "val0");
        assertEquals(1, cache.size());
        cache.add("key1", "val1");
        assertEquals(2, cache.size());
        cache.add("key2", "val2");
        assertEquals(2, cache.size());
    }
}