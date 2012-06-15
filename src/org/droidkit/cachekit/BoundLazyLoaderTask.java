package org.droidkit.cachekit;

import android.view.View;

public abstract class BoundLazyLoaderTask {
    
    private View mView;
    private Object mResultObject;
    

    public BoundLazyLoaderTask(View view) {
        mView = view;
        if (view == null)
            throw new NullPointerException("cannot pass a null view to BoundLazyLoaderTask");
    }
    
    public Object getResultObject() {
        return mResultObject;
    }
    
    public void setResultObject(Object resultObject) {
        mResultObject = resultObject;
    }
    
    public View getView() {
        return mView;
    }
    
    /**
     * @return The cache key to lookup/store the object. This key should be 100% unique 
     * to the Object being returned in loadInBackground(). For example, if you're using
     * the lazy loader to fetch remote bitmaps, the key should be the url of that bitmap 
     * (assuming you're not doing anything else to the bitmap before retruning it).
     * 
     *   This key will also be set as the tag on the view. This is done to ensure that the
     *   lazy loader never binds the wrong object to a view.
     */
    public abstract String getObjectKey();
    
    /**
     * do the actual lazy loading in this method
     */
    public abstract Object loadInBackground();
    
    public abstract void onLoadingStarted(View v);
    
    /**
     * Called on the UI thread after the lazy loading has completed
     */
    public abstract void onLoadComplete(View view, Object resultObject);
    
    /**
     * This is your chance to check if this task is valid (i.e. if your loading a remote image
     *  you can check to make sure your URL isn't null). Putting that logic in this method
     *  can help simplify your adapters.
     * @return false if it should not be added to the taskQueue, true otherwise
     */
    public abstract boolean isTaskValid();
}

