package org.droidkit.cachekit;

import java.util.ArrayList;

import org.droidkit.DroidKit;
import org.droidkit.util.tricks.CLog;

import android.app.ActivityManager;
import android.content.Context;
import android.view.View;


public class BoundLazyLoaderManager {
	
	protected ArrayList<BoundLazyLoader> mLazyLoaders;
	
	public BoundLazyLoaderManager(int numLoaders) {
		if (numLoaders <= 0)
			throw new IllegalArgumentException("cant create a BoundLazyLoaderManager with less than one thread");
		mLazyLoaders = new ArrayList<BoundLazyLoader>(numLoaders);
		
		ActivityManager am = (ActivityManager) DroidKit.getSystemService(Context.ACTIVITY_SERVICE);
		int heapMb = am.getMemoryClass();
		long maxSize = (long)(heapMb/2) * 1000L * 1000L; 
		
		if (DroidKit.DEBUG) CLog.e("MEMORY CLASS = " + heapMb + ", max cache size = " + maxSize);
		
		for (int i = 0; i<numLoaders; i++)
			mLazyLoaders.add(new BoundLazyLoader(maxSize));
	}
	
	public void addTask(BoundLazyLoaderTask task, int threadIndex) {
		addTask(task, false, threadIndex);
	}
	
	public void addTask(BoundLazyLoaderTask task, boolean shortDelay, int threadIndex) {
		mLazyLoaders.get(threadIndex).addTask(task, shortDelay);
	}
	
	public void clearQueues() {
		for (BoundLazyLoader l : mLazyLoaders)
			l.clearQueue();
	}
	
	public void cleanCache() {
		mLazyLoaders.get(0).triggerClean();
	}
	
	public void onResume(int threadIndex) {
		mLazyLoaders.get(threadIndex).onResume();
	}
	
	public void onViewDestroyed(View v) {
		for (int i =0; i<mLazyLoaders.size(); i++) {
			if (i == 0) {
				mLazyLoaders.get(i).onViewDestroyed(v, false);
				mLazyLoaders.get(i).triggerClean();
			} else {
				mLazyLoaders.get(i).clearViewHeierarchyFromQueue(v);
			}
		}
	}
	
	public void shutdown() {
		for (BoundLazyLoader l : mLazyLoaders)
			l.shutdown();
	}

}
