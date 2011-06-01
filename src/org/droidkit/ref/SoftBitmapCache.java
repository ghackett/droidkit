package org.droidkit.ref;

import java.util.HashMap;

import android.graphics.Bitmap;

public class SoftBitmapCache {
    protected HashMap<String, BitmapSoftReference> mCache;
    
    public SoftBitmapCache() {
        mCache = new HashMap<String, BitmapSoftReference>();
    }
    
    public void clearCache() {
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
}
