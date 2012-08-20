package org.droidkit.cachekit;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

public abstract class BitmapBoundLazyLoaderTask extends BoundLazyLoaderTask {

    public BitmapBoundLazyLoaderTask(ImageView view) {
        super(view);
    }

    @Override
    public Object loadInBackground() throws Throwable {
        return loadBitmapInBackground();
    }

    @Override
    public void onLoadingStarted(View v) {
        onBitmapLoadingStarted((ImageView)v);
    }

    @Override
    public void onLoadComplete(View view, Object resultObject) {
        if (view != null)
            onBitmapLoadComplete((ImageView)view, (Bitmap)resultObject);
    }
    
    public void onBitmapLoadComplete(ImageView view, Bitmap resultBitmap) {
        if (resultBitmap != null && !resultBitmap.isRecycled()) {
            view.setImageBitmap(resultBitmap);
        }
    }
    
    public abstract Bitmap loadBitmapInBackground() throws Throwable;
    
    public abstract void onBitmapLoadingStarted(ImageView v);

}
