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
     * shouldAddTask gets called inSync when the task is added to the LazyImageLoader
     * @return true if the task should be added to the processing stack, false if you 
     * took care of it in this method
     */
//    public boolean shouldAddTask();
    
    /**
     * @return the View that should be used to knock this task or
     * other tasks out of the processing stack - also its tag will be set
     * and then compared to at the end of the process
     */
//    public abstract View getView();
    
    /**
     * @return the tag that will be assigned to the view and then compared against
     * before calling onLoadComplete - this should be 100% unique
     */
    public abstract String getViewTag();
    
    /**
     * do the actual lazy loading in this method
     */
    public abstract Object loadInBackground();
    
    public abstract void onLoadingStarted(View v);
    
    /**
     * Called on the UI thread after the lazy loading has completed
     */
    public abstract void onLoadComplete(View view, Object resultObject);
}

