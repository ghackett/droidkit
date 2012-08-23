package org.droidkit.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.droidkit.DroidKit;

import android.app.Activity;


public class ActivityLifeCycleManager {
	
	protected static final long TIMEOUT = 5000;
	
	//store created activities as weak references cause your paused activity 
	//could be removed from memory, but you should always get an onPause
	protected ArrayList<WeakReference<Activity>> mCreatedActivities = null;
	protected ArrayList<Activity> mResumedActivities = null;
	protected boolean mHasCreatedActivities = false;
	protected boolean mHasResumedActivities = false;
	
	protected ActivityLifeCycleManager() {
		mCreatedActivities = new ArrayList<WeakReference<Activity>>();
		mResumedActivities = new ArrayList<Activity>();
	}
	
	protected final void flushBadReferences() {
		removeFromCreatedArray(null);
	}
	
	public void flush() {
		mCreatedActivities.clear();
		mResumedActivities.clear();
		mHasCreatedActivities = false;
		mHasResumedActivities = false;
	}
	
	public void onActivityCreate(Activity a) {
		DroidKit.getHandler().removeCallbacks(mOnDestroyRunnable);
		if (mCreatedActivities.size() > 0) {
			flushBadReferences();
			mHasCreatedActivities = !mCreatedActivities.isEmpty();
		}
		mCreatedActivities.add(new WeakReference<Activity>(a));
		if (!mHasCreatedActivities) {
			mHasCreatedActivities = true;
			onFirstActivityCreated();
		}
	}
	
	public void onActivityResume(Activity a) {
		DroidKit.getHandler().removeCallbacks(mOnPauseRunnable);
		if (mResumedActivities.size() > 0) {
			flushBadReferences();
			mHasResumedActivities = !mResumedActivities.isEmpty();
		}
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
		removeFromCreatedArray(a);
		if (mCreatedActivities.size() <= 0) {
			DroidKit.getHandler().postDelayed(mOnDestroyRunnable, TIMEOUT);
		}
	}
	
	private void removeFromCreatedArray(Activity a) {
		for (int i = 0; i<mCreatedActivities.size();) {
			WeakReference<Activity> ref = mCreatedActivities.get(i);
			Activity compare = ref.get();
			if (compare == null || compare == a)
				mCreatedActivities.remove(i);
			else
				i++;
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
		if (mHasCreatedActivities && mCreatedActivities.size() > 0) {
			flushBadReferences();
			mHasCreatedActivities = !mCreatedActivities.isEmpty();
		}
		return mHasCreatedActivities;
	}
	
	public boolean isAnActivityResumed() {
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
			flushBadReferences();
			if (mCreatedActivities.size() <= 0) {
				mHasCreatedActivities = false;
				onLastActivityDestroyed();
			}
		}
	};
}
