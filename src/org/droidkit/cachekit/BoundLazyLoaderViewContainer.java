package org.droidkit.cachekit;

import java.util.HashSet;

import android.view.View;
import android.widget.AbsListView.RecyclerListener;

public class BoundLazyLoaderViewContainer implements RecyclerListener {
    
    private HashSet<View> mRecycledViews;
    private BoundLazyLoader mLazyLoader;
    
    public BoundLazyLoaderViewContainer(BoundLazyLoader lazyLoader) {
        mRecycledViews = new HashSet<View>();
        mLazyLoader = lazyLoader;
    }

    @Override
    public void onMovedToScrapHeap(View view) {
        addView(view);
    }
    
    public void addView(View v) {
    	mRecycledViews.add(v);
    }
    
    //call this method in your Activity.onDestroy or Fragment.onDestroyView
    //to flush the bindings
    public void clearBindings(boolean inSync) {
        for (View v : mRecycledViews) {
            mLazyLoader.onViewDestroyed(v, inSync);
        }
        mRecycledViews.clear();
    }

}
