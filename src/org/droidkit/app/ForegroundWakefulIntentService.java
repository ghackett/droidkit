package org.droidkit.app;

import android.app.Notification;
import android.content.Intent;

public abstract class ForegroundWakefulIntentService extends
        WakefulIntentService {
    
    protected Notification mNotification = null;

    public ForegroundWakefulIntentService(String name) {
        super(name);
    }
    
    protected abstract int getNotificationId();
    protected abstract Notification getNotification();
    protected abstract void onHandleNewIntent(Intent intent);

    @Override
    protected void onHandleIntent(Intent intent) {
        mNotification = getNotification();
        startForeground(getNotificationId(), mNotification);
        onHandleNewIntent(intent);
        stopForeground(true);
    }

}
