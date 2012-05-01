package org.droidkit.cachekit;

import java.util.Stack;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

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
            if (object != null && object instanceof Bitmap) {
                Bitmap b = (Bitmap)object;
                if (!b.isRecycled())
                    b.recycle();
            }
        }
        
    };
    
//    public static boolean hasInstance() {
//        return sInstance != null;
//    }
    
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
        if (task == null)
            return;
        
        task.getView().setTag(task.getViewTag());
        
        
        
        if (shouldAddTask(task)) {
            synchronized (mTaskQueue) {
                for (int i = 0; i<mTaskQueue.size();) {
                    BoundLazyLoaderTask waitingTask = mTaskQueue.get(i);
                    if (waitingTask.getView() == task.getView()) {
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
                resetLoadTimer();
            }
        }
    }
    
    private boolean shouldAddTask(BoundLazyLoaderTask task) {
        Object obj = sCache.bind(task.getViewTag(), task.getView(), false);
        if (obj != null) {
            task.setResultObject(obj);
            task.onLoadComplete(obj);
            return false;
        }
        return true;
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
    
    private void resetLoadTimer() {
        resetLoadTimer(mDelay);
    }
    
    private void resetLoadTimer(long timer) {
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
                    if (mPaused)
                        return;
                    
                    BoundLazyLoaderTask task = null;
                    synchronized (mTaskQueue) {
                        if (mTaskQueue.size() > 0) {
                            task = mTaskQueue.pop();
                        }
                    }
                    
                    if (task != null && task.getView() != null && task.getViewTag().equals((String)task.getView().getTag())) {
                        try {
                            task.setResultObject(task.loadInBackground());
                            if (task.getResultObject() != null)
                                sCache.put(task.getViewTag(), task.getView(), task.getResultObject(), true);
                            sCache.cleanCache();
                            mUiHandler.post(new UINotifierTask(task));
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                    
                    
                    synchronized (mTaskQueue) {
                        if (mTaskQueue.size() > 0 && mThreadHandler != null)
                            resetLoadTimer(10);
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
                if (mTask.getView() != null && mTask.getViewTag().equals((String)mTask.getView().getTag())) {
                    mTask.onLoadComplete(mTask.getResultObject());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
