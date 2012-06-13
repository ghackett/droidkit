package org.droidkit.cachekit;

import java.util.ArrayList;
import java.util.Stack;

import org.droidkit.DroidKit;
import org.droidkit.util.tricks.CLog;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

public class BoundLazyLoader {
    
    private static BoundLazyLoader sInstance = null;
    
    private static final Object sLock = new Object();
    
    public static BoundLazyLoader get() {
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new BoundLazyLoader();
            }
        }
        return sInstance;
    }
    
    private static BoundCache<String, View, Object> sCache = new BoundCache<String, View, Object>() {

        @Override
        protected void onObjectUnbound(Object object) {
            super.onObjectUnbound(object);
            if (DroidKit.DEBUG) CLog.e("Unbound object");
            if (object != null && object instanceof Bitmap) {
                Bitmap b = (Bitmap)object;
                if (!b.isRecycled()) {
                    if (DroidKit.DEBUG) CLog.e("RECYCLE!!!");
                    b.recycle();
                }
            }
        }
        
    };
    
    
    public static void shutdownInstance() {
        synchronized (sLock) {
            if (sInstance != null) {
                get().shutdown();
                sInstance = null;
            }
        }
    }
    
    private Handler mThreadHandler = null;
    private final Handler mUiHandler = new Handler();
    private Stack<BoundLazyLoaderTask> mTaskQueue;
    private boolean mPaused = false;
    private int mDelay = 500;
    private ArrayList<View> mViewsToDestroy = new ArrayList<View>();
    private int mInOrderCount = 0;

    
    public BoundLazyLoader(int delay) {
        mDelay = delay;
        mTaskQueue = new Stack<BoundLazyLoaderTask>();
        
        LazyLoaderThread t = new LazyLoaderThread();
        t.setPriority(Math.max(Thread.MIN_PRIORITY, Thread.NORM_PRIORITY-3));
        t.start();
    }
    
    public BoundLazyLoader() {
        this(500);
    }

    public void setDelay(int delay) {
        mDelay = delay;
    }
    
    public void addTask(BoundLazyLoaderTask task) {
        addTask(task, false);
    }
    
    public void addTask(BoundLazyLoaderTask task, boolean shortDelay) {
        if (task == null)
            return;
        
        String key = task.getObjectKey();
        View view = task.getView();
        
        if (TextUtils.isEmpty(key))
            return;
        
        if (view == null)
            return;
        
//        if (key.equals(view.getTag()))
//            return;
        
        view.setTag(key);
        
        if (shouldAddTask(task)) {
            synchronized (mTaskQueue) {
                for (int i = 0; i<mTaskQueue.size();) {
                    BoundLazyLoaderTask waitingTask = mTaskQueue.get(i);
                    View waitingView = waitingTask.getView();
                    if (waitingView == view) {
//                        if (key.equals(waitingTask.getObjectKey()))
//                            return;
                        mTaskQueue.remove(i);
                    } else {
                        i++;
                    }
                }
                mTaskQueue.push(task);
            }
            
            
            if (mThreadHandler == null) {
                mUiHandler.postDelayed(mRetrySendMessageTask, mDelay);
            } else { 
                if (DroidKit.DEBUG) CLog.e("resetting load timer");
                if (shortDelay)
                    resetLoadTimer(10);
                else
                    resetLoadTimer();
            }
        }
    }
    
    /**
     * Call this in your fragment or activity's onResume, and the next 3 
     * lazy load tasks will be loaded in order, instead of reverse order
     * (if the queue is already empty)
     */
    public void onResume() {
        synchronized (mTaskQueue) {
            if (mTaskQueue.isEmpty())
                mInOrderCount = 3;
            else
                mInOrderCount = 0;
        }
    }
    
    private boolean shouldAddTask(BoundLazyLoaderTask task) {
        if (!mPaused) {
            Object obj = sCache.bind(task.getObjectKey(), task.getView(), false);
            if (obj != null) {
                task.setResultObject(obj);
                task.onLoadComplete(task.getView(), obj);
                if (mInOrderCount > 0)
                    mInOrderCount--;
                return false;
            }
        }
        task.onLoadingStarted(task.getView());
        return true;
    }
    
    /**
     * This will traverse your view tree and remove any view binders that it
     * contains (triggering a cleanCache in 500ms).
     * 
     * ONLY USE THIS METHOD IN YOUR Activity.onDestroy or Fragment.onDestroyView METHODS!
     * @param v the root of your view tree
     */
    public void onViewDestroyed(View v, boolean inSync) {
        if (v == null)
            return;
        if (inSync) {
            destroyView(v);
        } else {
            synchronized (mViewsToDestroy) {
                mViewsToDestroy.add(v);
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup p = (ViewGroup) v;
            int childCount = p.getChildCount();
            for (int i = 0; i<childCount; i++) {
                onViewDestroyed(p.getChildAt(i), inSync);
            }
        }
        resetLoadTimer();
    }
    
    private void destroyView(View v) {
//        v.setTag(null);
        synchronized (mTaskQueue) {
            for (int i = 0; i<mTaskQueue.size();) {
                BoundLazyLoaderTask waitingTask = mTaskQueue.get(i);
                if (waitingTask.getView() == v) {
                    mTaskQueue.remove(i);
                } else {
                    i++;
                }
            }
        }
        
        sCache.destroyBinder(v, false);
    }



    
    
    public void clearQueue() {
        synchronized (mTaskQueue) {
            mTaskQueue.clear();
        }
    }
    
    public void setPaused(boolean paused) {
        if (paused != mPaused) {
            mPaused = paused;
            if (mThreadHandler != null) {
                mThreadHandler.removeMessages(0);
                if (!mPaused) {
                    mThreadHandler.sendEmptyMessage(0);
                }
            } else if (!mPaused) {
                mUiHandler.postDelayed(mRetrySendMessageTask, mDelay);
            }
        }
    }
    
    public void shutdown() {
        if (mThreadHandler != null) {
            mThreadHandler.getLooper().quit();
            mThreadHandler = null;
        }
    }
    
    public void resetLoadTimer() {
        resetLoadTimer(mDelay);
    }
    
    public void resetLoadTimer(long timer) {
        try {
            mThreadHandler.removeMessages(0);
            mThreadHandler.sendEmptyMessageDelayed(0, timer);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    
    
    
    
    
    private final Runnable mRetrySendMessageTask = new Runnable() {
        
        @Override
        public void run() {
            if (mThreadHandler != null) {
                resetLoadTimer();
            } else {
                mUiHandler.postDelayed(mRetrySendMessageTask, mDelay);
            }
        }
    };
    
    private class LazyLoaderThread extends Thread {
        
        @Override
        public void run() {
            Looper.prepare();
            mThreadHandler = new Handler() {
                
                @Override
                public void handleMessage(Message msg) {

                    
                    boolean returnNow = false;
                    synchronized (mViewsToDestroy) {
                        if (!mViewsToDestroy.isEmpty()) {
                            returnNow = true;
                            for (View v : mViewsToDestroy) {
                                destroyView(v);
                            }
                            mViewsToDestroy.clear();
                        }
                    }
                    
                    if (returnNow) {
                        resetLoadTimer();
                        return;
                    }
                    
                    if (mPaused)
                        return;
                    
                    BoundLazyLoaderTask task = null;
                    synchronized (mTaskQueue) {
                        int size = mTaskQueue.size();
                        if (size > 0) {
                            if (mInOrderCount > 0) {
                                mInOrderCount--;
                                task = mTaskQueue.remove(0);
                            } else {
                                task = mTaskQueue.pop();
                            }
                            
                        }
                    }
                    
                    if (task != null && task.getView() != null && task.getObjectKey().equals((String)task.getView().getTag())) {
                        try {
                            Object obj = sCache.bind(task.getObjectKey(), task.getView(), true);
                            if (obj != null) {
                                task.setResultObject(obj);
                            } else {
                                task.setResultObject(task.loadInBackground());
                                if (task.getResultObject() != null)
                                    sCache.put(task.getObjectKey(), task.getView(), task.getResultObject(), true);
                            }
//                            sCache.cleanCache();
                            mUiHandler.post(new UINotifierTask(task));
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                        if (mThreadHandler != null)
                            resetLoadTimer(10);
                    } else {
                        sCache.cleanCache();
                        synchronized (mTaskQueue) {
                            if (mTaskQueue.size() > 0 && mThreadHandler != null)
                                resetLoadTimer(10);
                        }
                    }
                    
                    

                }
                
            };
            Looper.loop();
            super.run();
        }
    }
    
    private class UINotifierTask implements Runnable {

        BoundLazyLoaderTask mTask = null;
        
        public UINotifierTask(BoundLazyLoaderTask task) {
            mTask = task;
        }
        
        @Override
        public void run() {
            try {
                if (mTask.getView() != null && mTask.getObjectKey().equals((String)mTask.getView().getTag())) {
                    mTask.onLoadComplete(mTask.getView(), mTask.getResultObject());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
