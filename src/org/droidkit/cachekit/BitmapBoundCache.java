package org.droidkit.cachekit;

import android.graphics.Bitmap;

public class BitmapBoundCache<K, B> extends BoundCache<K, B, Bitmap> {

    @Override
    protected void onObjectUnbound(Bitmap object) {
        super.onObjectUnbound(object);
        if (object != null)
            object.recycle();
    }

    
    
}
