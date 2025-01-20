package org.motadata.services;

import io.vertx.core.json.JsonObject;

public class CacheEntry
{
    private final JsonObject value;

    private final long timestamp;

    public CacheEntry(JsonObject value, long timestamp)
    {
        this.value = value;

        this.timestamp = timestamp;
    }

    public JsonObject getValue()
    {
        return value;
    }

    public boolean isExpired(long ttl)
    {
        return (System.currentTimeMillis() - timestamp) >= ttl;
    }
}
