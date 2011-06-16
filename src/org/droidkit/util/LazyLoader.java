package org.droidkit.util;

import java.util.Stack;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class LazyLoader {
    
    private static LazyLoader sInstance = null;
    
    public static LazyLoader get() {
        if (sInstance == null) {
            sInstance = new LazyLoader();
        }
        return sInstance;
    }
    
    public static boolean hasInstance() {
        return sInstance != null;
    }
    
    public static void shutdownInstance() {
        if (hasInstance()) {
            get().shutdown();
            sInstance = null;
        }
    }
    
    private Handler mThreadHandler = null;
    private final Handler mUiHandler = new Handler();
    private Stack<LazyLoaderTask> mTaskQueue;
    
    private LazyLoader() {
        mTaskQueue = new Stack<LazyLoaderTask>();
        
        LazyLoaderThread t = new LazyLoaderThread();
        t.setPriority(Math.max(Thread.MIN_PRIORITY, Thread.NORM_PRIORITY-3));
        t.start();
    }

    public void addTask(LazyLoaderTask task) {
        if (task == null)
            return;
        
        if (task.shouldAddTask()) {
            task.getView().setTag(task.getViewTag());
            synchronized (mTaskQueue) {
                for (int i = 0; i<mTaskQueue.size();) {
                    LazyLoaderTask waitingTask = mTaskQueue.get(i);
                    if (waitingTask.getView() == task.getView()) {
                        mTaskQueue.remove(i);
                    } else {
                        i++;
                    }
                }
                mTaskQueue.push(task);
            }
            
            
            if (mThreadHandler == null) {
                mUiHandler.postDelayed(mRetrySendMessageTask, 500);
            } else { 
                resetLoadTimer();
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
        resetLoadTimer(500);
    }
    
    private void resetLoadTimer(long timer) {
        mThreadHandler.removeMessages(0);
        mThreadHandler.sendEmptyMessageDelayed(0, timer);
    }
    
    
    
    
    
    
    private final Runnable mRetrySendMessageTask = new Runnable() {
        
        @Override
        public void run() {
            if (mThreadHandler != null) {
                resetLoadTimer();
            } else {
                mUiHandler.postDelayed(mRetrySendMessageTask, 500);
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
                    LazyLoaderTask task = null;
                    synchronized (mTaskQueue) {
                        if (mTaskQueue.size() > 0) {
                            task = mTaskQueue.pop();
                        }
                    }
                    
                    if (task != null) {
                        try {
                            task.loadInBackground();
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

        LazyLoaderTask mTask = null;
        
        public UINotifierTask(LazyLoaderTask task) {
            mTask = task;
        }
        
        @Override
        public void run() {
            try {
                if (mTask.getView() != null && mTask.getViewTag().equals((String)mTask.getView().getTag())) {
                    mTask.onLoadComplete();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
