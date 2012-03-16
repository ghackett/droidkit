package org.droidkit.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

public abstract class ForegroundWakefulIntentService extends
        WakefulIntentService {
    
    protected Notification mNotification = null;

    public ForegroundWakefulIntentService(String name, boolean intentRedelivery) {
        super(name, intentRedelivery);
    }
    
    protected abstract int getNotificationId();
    protected abstract Notification getNotification();    
    

    @Override
    protected synchronized void lock() {
        super.lock();
        if (mNotification == null) {
            mNotification = getNotification();
            startForeground(getNotificationId(), mNotification);
        }
    }

    @Override
    protected synchronized void unlock() {
        stopForeground(true);
        ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(getNotificationId());
        super.unlock();
    }

    
}
