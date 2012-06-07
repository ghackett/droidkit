package org.droidkit.cachekit;

import java.util.ArrayList;

public class CacheManager {

    public interface CacheInterface {
        public void clearCache();
    }
    
    private static ArrayList<CacheInterface> sCacheInstances = null;
    
    private static ArrayList<CacheInterface> getInstances() {
        if (sCacheInstances == null) {
            sCacheInstances = new ArrayList<CacheManager.CacheInterface>();
        }
        return sCacheInstances;
    }
    
    public static void registerCache(CacheInterface cache) {
        getInstances().add(cache);
    }
    
    public static void clearAllCaches() {
        for (CacheInterface cache : getInstances()) {
            cache.clearCache();
        }
    }
    
    public static void clearNonBoundCaches() {
        for (CacheInterface cache : getInstances()) {
            if (!(cache instanceof BoundCache))
                cache.clearCache();
        }
    }
    
}
