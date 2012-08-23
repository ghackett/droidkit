package org.droidkit.cachekit;

import android.view.View;
import android.widget.ImageView;


public abstract class BitmapPlusBoundLazyLoaderTask extends BoundLazyLoaderTask {
   
	public BitmapPlusBoundLazyLoaderTask(ImageView view) {
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
            onBitmapLoadComplete((ImageView)view, (BitmapPlus)resultObject);
    }
    
    public void onBitmapLoadComplete(ImageView view, BitmapPlus result) {
    	if (result != null && !result.isBitmapRecycled()) {
	        view.setImageBitmap(result.getBitmap());
    	}
    }
    
    public abstract BitmapPlus loadBitmapInBackground() throws Throwable;
    
    public abstract void onBitmapLoadingStarted(ImageView v);
}
