package org.droidkit.cachekit;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import org.droidkit.cachekit.CacheManager.CacheInterface;

/**
 * A helper class used to cache objects inside SoftReferences
 * @author ghackett
 *
 */
public class SoftCache<T> implements CacheInterface {
    protected HashMap<String, SoftReference<T>> mCache;
    
    public SoftCache() {
        mCache = new HashMap<String, SoftReference<T>>();
        CacheManager.registerCache(this);
    }
    
    @Override
    public void clearCache() {
        mCache.clear();
    }
    
    public void put(String key, T obj) {
        if (obj == null)
            mCache.put(key, null);
        else
            mCache.put(key, new SoftReference<T>(obj));
    }
    
    public CacheResult<T> get(String key) {
        if (!mCache.containsKey(key)) {
            return new CacheResult<T>(null, false);
        }
        
        SoftReference<T> ref = mCache.get(key);
        if (ref == null) {
            return new CacheResult<T>(null, true);
        }
        
        T obj = ref.get();
        if (obj == null) {
            return new CacheResult<T>(null, false);
        } else {
            return new CacheResult<T>(obj, true);
        }
    }
    
    public int size() {
        return mCache.size();
    }
    
    public boolean isEmpty() {
        return mCache.isEmpty();
    }
}
