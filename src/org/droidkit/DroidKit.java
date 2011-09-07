package org.droidkit;

import java.io.File;
import java.util.Locale;

import org.droidkit.net.HttpConnectionMonitor;
import org.droidkit.ref.CacheManager;
import org.droidkit.util.LazyLoader;
import org.droidkit.util.tricks.StorageTricks;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;

public class DroidKit {
    
    private static final String SDCARD_PATH_FORMAT = "Android/data/%s";
//    private static final String ANDROID_MARKET_PACKAGE_NAME = "com.android.vending";
    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    
    private static Context sApplicationContext = null;
    private static ContentResolver sContentResolver = null;
    private static String sPackageName = null;
    private static Float sScreenDensity = null;
    private static Float sTextScale = null;
    private static File sExternalStorageDirectory = null;
    private static File sBestStorageDirectory = null;
    private static LayoutInflater sLayoutInflater = null;
    private static Boolean sCanAcceptPush = null;
    private static PackageInfo sPackageInfo = null;
    private static AccountManager sAccountManager = null;
    private static OnAccountsUpdateListener sAccountListener = null;
    private static HttpConnectionMonitor sConnectionMonitor = null;
    private static TelephonyManager sTelephonyManager = null;
    private static Handler sHandler = null;
    private static Boolean mGoogleMapsSupported = null;
    
    public static void onApplicationCreate(Context context) {
        if (sApplicationContext == null) {
            sApplicationContext = context.getApplicationContext();
        }
        if (sHandler == null) {
            sHandler = new Handler();
        }
    }
    
    public static void onApplicationTerminate() {
        if (sAccountManager != null) {
            if (sAccountListener != null) {
                sAccountManager.removeOnAccountsUpdatedListener(sAccountListener);
                sAccountListener = null;
            }
            sAccountManager = null;
        }
        sApplicationContext = null;
        sContentResolver = null;
        sPackageName = null;
        sScreenDensity = null;
        sTextScale = null;
        sExternalStorageDirectory = null;
        sBestStorageDirectory = null;
        sLayoutInflater = null;
        sCanAcceptPush = null;
        sPackageInfo = null;
        sTelephonyManager = null;
        LazyLoader.shutdownInstance();
        CacheManager.clearAllCaches();
        sConnectionMonitor = null;
        sHandler = null;
    }
    
    public static Context getContext() {
        return sApplicationContext;
    }
    
    public static ContentResolver getContentResolver() {
        if (sContentResolver == null) {
            sContentResolver = sApplicationContext.getContentResolver();
        }
        return sContentResolver;
    }
    
    public static String getPackageName() {
        if (sPackageName == null) {
            sPackageName = sApplicationContext.getPackageName();
        }
        return sPackageName;
    }
    
    public static float getScreenDensity() {
        if (sScreenDensity == null) {
            sScreenDensity = getDisplayMetrics().density;
        }
        return sScreenDensity.floatValue();
    }
    
    public static float getTextScale() {
        if (sTextScale == null) {
            sTextScale = getDisplayMetrics().scaledDensity;
        }
        return sTextScale.floatValue();
    }
    
    public static File getExternalStorageDirectory() {
        if (sExternalStorageDirectory == null) {
            sExternalStorageDirectory = new File(Environment.getExternalStorageDirectory(), String.format(Locale.US, SDCARD_PATH_FORMAT, getPackageName()));
        }
        if (!sExternalStorageDirectory.exists()) {
            sExternalStorageDirectory.mkdirs();
        }
        return sExternalStorageDirectory;
    }
    
    public static File getBestStorageDirectory(boolean includeAppDirectory) {
        if (sBestStorageDirectory == null) {
            File bestWritable = StorageTricks.findWritableDirectoryWithMostFreeSpace(getContext(), includeAppDirectory);
            if (bestWritable == null)
                return null;
            sBestStorageDirectory = new File(bestWritable, String.format(Locale.US, SDCARD_PATH_FORMAT, getPackageName()));
        }
        
        if (sBestStorageDirectory == null)
            return null;
                
        if (!sBestStorageDirectory.exists()) {
            sBestStorageDirectory.mkdirs();
        }
        
        if (!sBestStorageDirectory.canWrite())
            return null;

        
        return sBestStorageDirectory;
    }
    
    public static LayoutInflater getLayoutInflater() {
        if (sLayoutInflater == null) {
            sLayoutInflater = LayoutInflater.from(sApplicationContext);
        }
        return sLayoutInflater;
    }
    
    public static TelephonyManager getTelephonyManager() {
        if (sTelephonyManager == null)
            sTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return sTelephonyManager;
    }
    
    public static PackageInfo getPackageInfo() {
        if (sPackageInfo == null) {
            try {
                sPackageInfo = sApplicationContext.getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return sPackageInfo;
    }
    
    public static AccountManager getAccountManager() {
        if (sAccountManager == null)
            sAccountManager = AccountManager.get(getContext());
        return sAccountManager;
    }
    
    public static HttpConnectionMonitor getHttpConnectionMonitor() {
    	if (sConnectionMonitor == null)
    		sConnectionMonitor = new HttpConnectionMonitor();
    	return sConnectionMonitor;
    }
    
    public static Handler getHandler() {
        return sHandler;
    }
    
    public static String getVersionName() {
        return getPackageInfo().versionName;
    }
    
    public static int getVersionCode() {
        return getPackageInfo().versionCode;
    }
    
    public static void sendBroadcast(Intent intent) {
        sApplicationContext.sendBroadcast(intent);
    }
    
    public static Resources getResources() {
        return sApplicationContext.getResources();
    }
    
    public static DisplayMetrics getDisplayMetrics() {
        return getResources().getDisplayMetrics();
    }
    
    public static boolean isDeviceInPortraitMode() {
        DisplayMetrics metrics = getDisplayMetrics();
        return metrics.heightPixels > metrics.widthPixels;
    }
    
    public static boolean isDeviceInLandscapeMode() {
        DisplayMetrics metrics = getDisplayMetrics();
        return metrics.widthPixels >= metrics.heightPixels;
    }
    
    public static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(sApplicationContext);
    }
    
    public static String getString(int resource) {
        return sApplicationContext.getString(resource);
    }

    public static String getString(int resource, Object... formatArgs) {
        return sApplicationContext.getString(resource, formatArgs);
    }
    
    public static int getColor(int colorResId) {
        return getResources().getColor(colorResId);
    }

    public static int getResourceId(String name) {
        return getResources().getIdentifier(name, null, sApplicationContext.getPackageName());
    }

    public static Drawable getDrawable(int resource) {
        return getResources().getDrawable(resource);
    }

    public static Bitmap getBitmap(int resource, int widthDp, int heightDp) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.outWidth = getPixels(widthDp);
        opts.outHeight = getPixels(heightDp);
        return BitmapFactory.decodeResource(getResources(), resource, opts);
    }
    
    public static Bitmap getBitmap(int resource) {
        return BitmapFactory.decodeResource(getResources(), resource);
    }
    
    public static int getPixels(int dip) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getDisplayMetrics());
    }
    
    public static boolean isApplicationInstalled(String packageName) {
        boolean isInstalled = false;
        try {
            sApplicationContext.getPackageManager().getPackageInfo(packageName, 0);
            isInstalled = true;
        } catch (NameNotFoundException e) {
            isInstalled = false;
        }
        return isInstalled;
    }
    
    public static boolean isDevicePushCapable() {
        if (sCanAcceptPush == null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                sCanAcceptPush = false;
            } else {
                sCanAcceptPush = getAccountManager().getAccountsByType(GOOGLE_ACCOUNT_TYPE).length > 0;
                if (sAccountListener == null) {
                    //register an OnAccountsUpdateListener that nulls out the cached sCanAcceptPush value
                    //in case a google account is added or removed while the app is open. This listener is 
                    //removed in DroidKit's onApplicationTerminate
                    sAccountListener = new OnAccountsUpdateListener() {
                        
                        @Override
                        public void onAccountsUpdated(Account[] accounts) {
                            sCanAcceptPush = getAccountManager().getAccountsByType(GOOGLE_ACCOUNT_TYPE).length > 0;
                        }
                    };
                    getAccountManager().addOnAccountsUpdatedListener(sAccountListener, null, true);
                }
            }
        }
        return sCanAcceptPush.booleanValue();
    }
    
    public static void registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission) {
        sApplicationContext.registerReceiver(receiver, filter, broadcastPermission, null);
    }
    
    public static void registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler handler) {
        sApplicationContext.registerReceiver(receiver, filter, broadcastPermission, handler);
    }
    
    public static void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        sApplicationContext.registerReceiver(receiver, filter);
    }
    
    public static boolean tryUnregisterReceiver(BroadcastReceiver receiver) {
        try {
            sApplicationContext.unregisterReceiver(receiver);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
    
    public static Object getSystemService(String name) {
        return sApplicationContext.getSystemService(name);
    }
    
    public static InputMethodManager getInputManager() {
        return (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    
    public static boolean isFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }
    
    public static boolean isHoneycomb() {
        // Can use static final constants like HONEYCOMB, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean isHoneycombTablet() {
        // Can use static final constants like HONEYCOMB, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return isHoneycomb() && (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                == Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }
    
    public static boolean isGoogleMapsSupported() {
        if (mGoogleMapsSupported == null) {
            try {
                Class.forName("com.google.android.maps.MapActivity");
                mGoogleMapsSupported = true;
            } catch (ClassNotFoundException e) {
                mGoogleMapsSupported = false;
            }
        }
        return mGoogleMapsSupported.booleanValue();
    }
    
    public static boolean getBoolPreference(String key, boolean defValue) {
        return getSharedPreferences().getBoolean(key, defValue);
    }
    
    public static int getIntPreference(String key, int defValue) {
        return getSharedPreferences().getInt(key, defValue);
    }
    
    public static float getFloatPreference(String key, float defValue) {
        return getSharedPreferences().getFloat(key, defValue);
    }
    
    public static long getLongPreference(String key, long defValue) {
        return getSharedPreferences().getLong(key, defValue);
    }
    
    public static String getStringPreference(String key, String defValue) {
        return getSharedPreferences().getString(key, defValue);
    }
    
    public static void setPreference(String key, boolean value) {
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        edit.putBoolean(key, value);
        edit.commit();
    }
    
    public static void setPreference(String key, int value) {
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        edit.putInt(key, value);
        edit.commit();
    }
    
    public static void setPreference(String key, float value) {
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        edit.putFloat(key, value);
        edit.commit();
    }
    
    public static void setPreference(String key, long value) {
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        edit.putLong(key, value);
        edit.commit();
    }
    
    public static void setPreference(String key, String value) {
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        edit.putString(key, value);
        edit.commit();
    }

    public static void clearPreferences() {
        getSharedPreferences().edit().clear().commit();
    }
}
