//package org.motadata.services;
//
//import io.vertx.core.json.JsonObject;
//import java.util.HashMap;
//import java.util.Map;
//
//public class CacheService {
//    private static final CacheService instance = new CacheService();
//    private final Map<String, JsonObject> cache = new HashMap<>();
//
//    private CacheService() {}
//
//    public static CacheService getInstance() {
//        return instance;
//    }
//
//    public boolean isCacheHit(String key) {
//        return cache.containsKey(key);
//    }
//
//    public JsonObject getCache(String key) {
//        return cache.get(key);
//    }
//
//    public void putCache(String key, JsonObject value) {
//        cache.put(key, value);
//    }
//}
