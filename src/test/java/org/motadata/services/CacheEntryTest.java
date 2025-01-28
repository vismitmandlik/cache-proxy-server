package org.motadata.services;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

public class CacheEntryTest
{
    @Test
    public void testCacheEntryConstructor()
    {
        var testValue = new JsonObject().put("key", "value");

        var timestamp = System.currentTimeMillis();

        var cacheEntry = new CacheEntry(testValue, timestamp);

        assertNotNull(cacheEntry, "CacheEntry should not be null");

        assertEquals(testValue, cacheEntry.getValue(), "CacheEntry value should match the input value");
    }

    @Test
    public void testIsExpired_NotExpired()
    {
        var testValue = new JsonObject().put("key", "value");

        var timestamp = System.currentTimeMillis();

        var cacheEntry = new CacheEntry(testValue, timestamp);

        // TTL of 1 minute
        var ttl = TimeUnit.MINUTES.toMillis(1);

        // Should not be expired immediately
        assertFalse(cacheEntry.isExpired(ttl), "Cache entry should not be expired immediately after creation");
    }

    @Test
    public void testIsExpired_Expired()
    {
        var testValue = new JsonObject().put("key", "value");

        var timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2);  // 2 minutes ago

        var cacheEntry = new CacheEntry(testValue, timestamp);

        // TTL of 1 minute
        var ttl = TimeUnit.MINUTES.toMillis(1);

        // Should be expired after 2 minutes
        assertTrue(cacheEntry.isExpired(ttl), "Cache entry should be expired after 2 minutes with 1 minute TTL");
    }
}
