package org.motadata.services;

import io.vertx.core.json.JsonObject;
import org.motadata.Main;
import org.motadata.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheService extends CacheEntry
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);

    private static final int INITIAL_CAPACITY = 16;

    private static final float LOAD_FACTOR = 0.75f;

    private static final boolean ACCESS_ORDER = true;

    private static final Map<String, CacheEntry> CACHE = new LinkedHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR, ACCESS_ORDER)
    {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest)
        {
            return size() > Main.CONFIG.getInteger(Constants.MAX_ENTRIES) || eldest.getValue().isExpired(Main.CONFIG.getLong(Constants.TTL));
        }
    };

    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    public CacheService(JsonObject value, long timestamp)
    {
        super(value, timestamp);
    }

    // Use inherited CacheEntry methods here if needed
    public static boolean isCacheHit(String key)
    {
        LOCK.readLock().lock();

        try
        {
            CacheEntry entry = CACHE.get(key);

            LOGGER.debug(CACHE.toString());

            return entry != null && !entry.isExpired(Main.CONFIG.getLong(Constants.TTL));
        }

        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage());

            return false;
        }

        finally
        {
            LOCK.readLock().unlock();
        }
    }

    public static JsonObject getCache(String key)
    {
        LOCK.readLock().lock();

        try
        {
            CacheEntry entry = CACHE.get(key);

            return (entry != null && !entry.isExpired(Main.CONFIG.getLong(Constants.TTL))) ? entry.getValue() : null;
        }
        finally
        {
            LOCK.readLock().unlock();
        }
    }

    public static void putCache(String key, JsonObject value)
    {
        LOCK.writeLock().lock();

        try
        {
            CACHE.put(key, new CacheEntry(value, System.currentTimeMillis()));
        }

        finally
        {
            LOCK.writeLock().unlock();
        }
    }

    public static void clearCache()
    {
        LOCK.writeLock().lock();

        try
        {
            CACHE.clear();

            LOGGER.info("Cache cleared. Cache size is now: {}", 0);

            LOGGER.info(CACHE.toString());
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage());
        }
        finally
        {
            LOCK.writeLock().unlock();
        }
    }

    public static class CacheClear
    {

        public static void main(String[] args)
        {

            clearCache();
        }
    }
}
