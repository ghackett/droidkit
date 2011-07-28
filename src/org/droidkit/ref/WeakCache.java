package org.droidkit.ref;


import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.droidkit.ref.CacheManager.CacheInterface;

/**
 * A helper class used to cache objects inside WeakReferences
 * @author ghackett
 *
 */
public class WeakCache<T> implements CacheInterface {
    protected HashMap<String, WeakReference<T>> mCache;
    
    public WeakCache() {
        mCache = new HashMap<String, WeakReference<T>>();
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
            mCache.put(key, new WeakReference<T>(obj));
    }
    
    public CacheResult<T> get(String key) {
        if (!mCache.containsKey(key)) {
            return new CacheResult<T>(null, false);
        }
        
        WeakReference<T> ref = mCache.get(key);
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
