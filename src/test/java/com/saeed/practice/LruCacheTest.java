package com.saeed.practice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void testNoInconsistentStateAfterConcurrentAccess() throws InterruptedException {
        LruCache<String, String> cache = new LruCache<>(100);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        IntStream.range(0, 10).forEach(i -> executor.submit(() -> {
            for (int j = 0; j < 500; j++) {
                String key = "key" + (j % 200);
                cache.add(key, "value" + j);
                cache.get(key);
            }
        }));

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        assertTrue(cache.size() <= 100);
    }

    @Test
    void testMultipleThreadsAddingSameKey() throws InterruptedException {
        LruCache<String, String> cache = new LruCache<>(10);
        ExecutorService executor = Executors.newFixedThreadPool(5);

        Runnable sameKeyWriter = () -> {
            for (int i = 0; i < 1000; i++) {
                cache.add("sharedKey", "val" + i);
            }
        };

        for (int i = 0; i < 5; i++) {
            executor.submit(sameKeyWriter);
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertTrue(cache.get("sharedKey").isPresent());
        assertEquals(1, cache.size());
    }

    @Test
    void testConcurrentAccessDoesNotThrowExceptions() throws InterruptedException {
        LruCache<String, String> cache = new LruCache<>(50);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(1);

        Runnable writer = () -> {
            try {
                latch.await();
                IntStream.range(0, 1000).forEach(i -> {
                    cache.add("key" + (i % 100), "val" + i);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable reader = () -> {
            try {
                latch.await();
                IntStream.range(0, 1000).forEach(i -> {
                    cache.get("key" + (i % 100));
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        for (int i = 0; i < 4; i++) executor.submit(writer);
        for (int i = 0; i < 4; i++) executor.submit(reader);

        latch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertTrue(cache.size() <= 50);
    }
}
