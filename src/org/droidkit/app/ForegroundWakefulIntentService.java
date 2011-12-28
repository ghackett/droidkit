package org.droidkit.app;

import android.app.Notification;

public abstract class ForegroundWakefulIntentService extends
        WakefulIntentService {
    
    protected Notification mNotification = null;

    public ForegroundWakefulIntentService(String name) {
        super(name);
    }
    
    protected abstract int getNotificationId();
    protected abstract Notification getNotification();    
    
    @Override
    public void onCreate() {
        super.onCreate();
        mNotification = getNotification();
        startForeground(getNotificationId(), mNotification);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

}
