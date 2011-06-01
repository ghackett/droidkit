package org.droidkit.ref;

import java.util.ArrayList;

public class SoftCacheManager {

    public interface SoftCacheInterface {
        public void clearCache();
    }
    
    private static ArrayList<SoftCacheInterface> sCacheInstances = null;
    
    private static ArrayList<SoftCacheInterface> getInstances() {
        if (sCacheInstances == null) {
            sCacheInstances = new ArrayList<SoftCacheManager.SoftCacheInterface>();
        }
        return sCacheInstances;
    }
    
    public static void registerCache(SoftCacheInterface cache) {
        getInstances().add(cache);
    }
    
    public static void clearAllCaches() {
        for (SoftCacheInterface cache : getInstances()) {
            cache.clearCache();
        }
    }
    
}
