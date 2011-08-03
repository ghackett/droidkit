package org.droidkit.ref;

import java.util.HashMap;

import org.droidkit.ref.CacheManager.CacheInterface;

import android.graphics.Bitmap;

public class WeakBitmapCache implements CacheInterface {
    protected HashMap<String, BitmapWeakReference> mCache;
    
    public WeakBitmapCache() {
        mCache = new HashMap<String, BitmapWeakReference>();
        CacheManager.registerCache(this);
    }
    
    @Override
    public void clearCache() {
        for (BitmapWeakReference ref : mCache.values())
            if (ref != null) ref.clear();
        mCache.clear();
    }
    
    public void put(String key, Bitmap obj) {
        if (obj == null)
            mCache.put(key, null);
        else
            mCache.put(key, new BitmapWeakReference(obj));
    }
    
    public CacheResult<Bitmap> get(String key) {
        if (!mCache.containsKey(key)) {
            return new CacheResult<Bitmap>(null, false);
        }
        
        BitmapWeakReference ref = mCache.get(key);
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
