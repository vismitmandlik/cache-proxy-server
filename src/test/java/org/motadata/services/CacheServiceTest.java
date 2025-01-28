package org.motadata.services;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.motadata.Main;
import org.motadata.constants.Constants;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class CacheServiceTest
{
    @BeforeEach
    void setUp(VertxTestContext testContext)
    {
        try {
            Main.initialize(new String[]{"--port", "8000", "--origin", "http://dummyjson.com"}).onComplete(result ->
            {
                if (result.succeeded())
                {
                    // Set up necessary system properties for testing cache
                    System.setProperty(Constants.MAX_ENTRIES, "5");

                    System.setProperty(Constants.TTL, "10000");

                    // Clear the cache to ensure a clean test environment
                    CacheService.clearCache();

                    testContext.completeNow();
                }
                else
                {
                    testContext.failNow(result.cause());
                }
            });
        }
        catch (Exception exception)
        {
            testContext.failNow(exception);
        }
    }

    @Test
    void testCachePutAndGet(VertxTestContext testContext)
    {
        // Prepare data
        var value = new JsonObject().put("key", "value");

        // Put data into cache
        CacheService.putCache("testKey", value);

        // Get the data from the cache
        var result = CacheService.getCache("testKey");

        // Assert the cache contains the value
        assertNotNull(result, "Cache should contain the value for the key.");

        assertEquals(value, result, "The value retrieved from cache should match the put value.");

        testContext.completeNow();
    }

    @Test
    void testCacheHit(VertxTestContext testContext)
    {
        // Prepare data
        var value = new JsonObject().put("key", "value");

        // Put data into cache
        CacheService.putCache("testKey", value);

        // Assert cache hit
        var isHit = CacheService.isCacheHit("testKey");

        // Assert the cache hit
        assertTrue(isHit, "Cache should have a hit for the key.");

        testContext.completeNow();
    }

    @Test
    void testCacheMiss(VertxTestContext testContext)
    {
        // Ensure no entry exists for a random key
        var isHit = CacheService.isCacheHit("nonExistingKey");

        // Assert cache miss
        assertFalse(isHit, "Cache should not have a hit for a non-existing key.");

        testContext.completeNow();
    }

    @Test
    void testCacheExpiry(VertxTestContext testContext) throws InterruptedException
    {
        // Prepare data
        var value = new JsonObject().put("key", "value");

        // Put data into cache
        CacheService.putCache("testKey", value);

        // Wait for TTL to pass (simulate expiration)
        Thread.sleep(11000);  // Simulate TTL expiration of 10000ms + 1000ms buffer

        // Assert cache miss after TTL expiration
        var result = CacheService.getCache("testKey");

        assertNull(result, "Cache should return null after the TTL expires.");

        testContext.completeNow();
    }

    @Test
    void testCacheClear(VertxTestContext testContext)
    {
        // Prepare data
        var value = new JsonObject().put("key", "value");

        // Put data into cache
        CacheService.putCache("testKey", value);

        // Assert the data is in the cache before clearing
        assertNotNull(CacheService.getCache("testKey"), "Cache should contain the value before clearing.");

        // Clear the cache
        CacheService.clearCache();

        // Assert the cache is empty after clearing
        assertNull(CacheService.getCache("testKey"), "Cache should be empty after clearing.");

        testContext.completeNow();
    }
}
