package org.droidkit.util;

import java.util.ArrayList;

import org.droidkit.DroidKit;

import android.app.Activity;


public class ActivityLifeCycleManager {
	
	protected static final long TIMEOUT = 5000;
	
	protected ArrayList<Activity> mCreatedActivities = null;
	protected ArrayList<Activity> mResumedActivities = null;
	protected boolean mHasCreatedActivities = false;
	protected boolean mHasResumedActivities = false;
	
	protected ActivityLifeCycleManager() {
		mCreatedActivities = new ArrayList<Activity>();
		mResumedActivities = new ArrayList<Activity>();
	}
	
	public void flush() {
		mCreatedActivities.clear();
		mResumedActivities.clear();
		mHasCreatedActivities = false;
		mHasResumedActivities = false;
	}
	
	public void onActivityCreate(Activity a) {
		DroidKit.getHandler().removeCallbacks(mOnDestroyRunnable);
		mCreatedActivities.add(a);
		if (!mHasCreatedActivities) {
			mHasCreatedActivities = true;
			onFirstActivityCreated();
		}
	}
	
	public void onActivityResume(Activity a) {
		DroidKit.getHandler().removeCallbacks(mOnPauseRunnable);
		mResumedActivities.add(a);
		if (!mHasResumedActivities) {
			mHasResumedActivities = true;
			onFirstActivityResumed();
		}
	}
	
	public void onActivityPause(Activity a) {
		DroidKit.getHandler().removeCallbacks(mOnPauseRunnable);
		mResumedActivities.remove(a);
		if (mResumedActivities.size() <= 0) {
			DroidKit.getHandler().postDelayed(mOnPauseRunnable, TIMEOUT);
		}
	}
	
	public void onActivityDestroy(Activity a) {
		DroidKit.getHandler().removeCallbacks(mOnDestroyRunnable);
		mCreatedActivities.remove(a);
		if (mCreatedActivities.size() <= 0) {
			DroidKit.getHandler().postDelayed(mOnDestroyRunnable, TIMEOUT);
		}
	}
	
	protected void onFirstActivityCreated() {
		
	}
	
	protected void onFirstActivityResumed() {
		
	}
	
	protected void onLastActivityPaused() {
		
	}
	
	protected void onLastActivityDestroyed() {
		
	}
	
	public boolean isAnActivityCreated() {
//		boolean result = false;
//		synchronized (mCreatedActivities) {
//			result = mCreatedActivities.size() > 0;
//		}
//		return result;
		return mHasCreatedActivities;
	}
	
	public boolean isAnActivityResumed() {
//		boolean result = false;
//		synchronized (mResumedActivities) {
//			result = mResumedActivities.size() > 0;
//		}
//		return result;
		return mHasResumedActivities;
	}
	
	private Runnable mOnPauseRunnable = new Runnable() {
		
		@Override
		public void run() {
			if (mResumedActivities.size() <= 0) {
				mHasResumedActivities = false;
				onLastActivityPaused();
			}
		}
	};

	private Runnable mOnDestroyRunnable = new Runnable() {
		
		@Override
		public void run() {
			if (mCreatedActivities.size() <= 0) {
				mHasCreatedActivities = false;
				onLastActivityDestroyed();
			}
		}
	};
}
