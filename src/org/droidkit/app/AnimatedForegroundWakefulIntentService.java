package org.droidkit.app;

import org.droidkit.DroidKit;

import android.app.NotificationManager;
import android.content.Context;

public abstract class AnimatedForegroundWakefulIntentService extends
        ForegroundWakefulIntentService {
    
    private int mCounter = 0;

    public AnimatedForegroundWakefulIntentService(String name, boolean intentRedelivery) {
        super(name, intentRedelivery);
    }
    
    protected abstract int getMaxIconLevel();
    protected abstract int getAnimationDelay();
    
    
    protected void animateIcon() {
        mCounter++;
        if (mCounter > getMaxIconLevel())
            mCounter = 0;
        if (mNotification != null) {
            mNotification.iconLevel = mCounter;
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(getNotificationId(), mNotification);
        }
    }
    
    private Runnable mAnimateTask = new Runnable() {
        
        @Override
        public void run() {
            animateIcon();
            DroidKit.getHandler().postDelayed(mAnimateTask, getAnimationDelay());
        }
    };

    @Override
    protected synchronized void lock() {
        super.lock();
        DroidKit.getHandler().removeCallbacks(mAnimateTask);
        DroidKit.getHandler().postDelayed(mAnimateTask, getAnimationDelay());
    }

    @Override
    protected synchronized void unlock() {
        DroidKit.getHandler().removeCallbacks(mAnimateTask);
        super.unlock();
    }
    
    

}
