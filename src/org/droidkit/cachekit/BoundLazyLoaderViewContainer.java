package org.droidkit.cachekit;

import java.util.HashSet;

import android.view.View;
import android.widget.AbsListView.RecyclerListener;

public class BoundLazyLoaderViewContainer implements RecyclerListener {
    
    private HashSet<View> mRecycledViews;
    private BoundLazyLoaderManager mLazyLoaderManager;
    
    public BoundLazyLoaderViewContainer(BoundLazyLoaderManager lazyLoaderManager) {
        mRecycledViews = new HashSet<View>();
        mLazyLoaderManager = lazyLoaderManager;
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
    public void clearBindings() {
        for (View v : mRecycledViews) {
            mLazyLoaderManager.onViewDestroyed(v);
        }
        mRecycledViews.clear();
    }

}
