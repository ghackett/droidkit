package org.droidkit.cachekit;

import java.util.ArrayList;

import android.view.View;


public class BoundLazyLoaderManager {
	
	protected ArrayList<BoundLazyLoader> mLazyLoaders;
	
	public BoundLazyLoaderManager(int numLoaders) {
		if (numLoaders <= 0)
			throw new IllegalArgumentException("cant create a BoundLazyLoaderManager with less than one thread");
		mLazyLoaders = new ArrayList<BoundLazyLoader>(numLoaders);
		for (int i = 0; i<numLoaders; i++)
			mLazyLoaders.add(new BoundLazyLoader());
	}
	
	public void addTask(BoundLazyLoaderTask task, int threadIndex) {
		
	}
	
	public void addTask(BoundLazyLoaderTask task, boolean shortDelay, int threadIndex) {
		
	}
	
	public void clearQueues() {
		
	}
	
	public void onResume(int threadIndex) {
		
	}
	
	public void onViewDestroyed(View v) {
		
	}

}
