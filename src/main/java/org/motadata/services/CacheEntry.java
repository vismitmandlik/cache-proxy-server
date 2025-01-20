package org.motadata.services;

import io.vertx.core.json.JsonObject;

public class CacheEntry
{
    private final JsonObject value;

    private final long timestamp;

    private final String source;

    private final String version;

    public CacheEntry(JsonObject value, long timestamp, String source, String version)
    {
        this.value = value;

        this.timestamp = timestamp;

        this.source = source;

        this.version = version;
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
