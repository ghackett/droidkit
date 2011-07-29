package org.droidkit.app;

import android.app.IntentService;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public abstract class WakefulIntentService extends IntentService {
    
    protected WakeLock mWakeLock;

    public WakefulIntentService(String name) {
        super(name);
    }
    
    public abstract String getTag();

    @Override
    public void onCreate() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getTag());
        mWakeLock.acquire();
        
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mWakeLock.release();
        super.onDestroy();
    }

}
