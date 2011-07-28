package org.droidkit.ref;

import java.util.HashMap;

import org.droidkit.ref.CacheManager.CacheInterface;

import android.graphics.Bitmap;

public class SoftBitmapCache implements CacheInterface {
    protected HashMap<String, BitmapSoftReference> mCache;
    
    public SoftBitmapCache() {
        mCache = new HashMap<String, BitmapSoftReference>();
        CacheManager.registerCache(this);
    }
    
    @Override
    public void clearCache() {
        for (BitmapSoftReference ref : mCache.values())
            ref.clear();
        mCache.clear();
    }
    
    public void put(String key, Bitmap obj) {
        if (obj == null)
            mCache.put(key, null);
        else
            mCache.put(key, new BitmapSoftReference(obj));
    }
    
    public CacheResult<Bitmap> get(String key) {
        if (!mCache.containsKey(key)) {
            return new CacheResult<Bitmap>(null, false);
        }
        
        BitmapSoftReference ref = mCache.get(key);
        if (ref == null) {
            return new CacheResult<Bitmap>(null, true);
        }
        
        Bitmap obj = ref.get();
        if (obj == null) {
            return new CacheResult<Bitmap>(null, false);
        } else {
            return new CacheResult<Bitmap>(obj, true);
        }
    }
    
    public int size() {
        return mCache.size();
    }
    
    public boolean isEmpty() {
        return mCache.isEmpty();
    }
}
