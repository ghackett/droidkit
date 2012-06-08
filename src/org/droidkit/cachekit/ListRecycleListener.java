package org.droidkit.cachekit;

import java.util.HashSet;

import android.view.View;
import android.widget.AbsListView.RecyclerListener;

public class ListRecycleListener implements RecyclerListener {
    
    private HashSet<View> mRecycledViews;
    private BoundLazyLoader mLazyLoader;
    
    public ListRecycleListener(BoundLazyLoader lazyLoader) {
        mRecycledViews = new HashSet<View>();
        mLazyLoader = lazyLoader;
    }

    @Override
    public void onMovedToScrapHeap(View view) {
        mRecycledViews.add(view);
    }
    
    public void clearBindings(boolean inSync) {
        for (View v : mRecycledViews) {
            mLazyLoader.onViewDestroyed(v, inSync);
        }
        mRecycledViews.clear();
    }

}
