package org.droidkit.cachekit;

public class CacheResult<T> {
    private T mObj = null;
    private boolean mCached = false;

    public CacheResult(T obj, boolean cached) {
        mObj = obj;
        mCached = cached;
    }
    
    public T getCachedObject() {
        return mObj;
    }
    
    public boolean isCached() {
        return mCached;
    }
}
