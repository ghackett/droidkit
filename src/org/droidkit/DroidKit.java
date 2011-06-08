package org.droidkit;

import java.io.File;
import java.util.Locale;

import org.droidkit.util.tricks.StorageTricks;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;

public class DroidKit {
    
    private static final String SDCARD_PATH_FORMAT = "Android/data/%s";
    private static final String ANDROID_MARKET_PACKAGE_NAME = "com.android.vending";
    
    private static Context sApplicationContext = null;
    private static ContentResolver sContentResolver = null;
    private static String sPackageName = null;
    private static Float sScreenDensity = null;
    private static Float sTextScale = null;
    private static File sExternalStorageDirectory = null;
    private static File sBestStorageDirectory = null;
    private static LayoutInflater sLayoutInflater = null;
    private static Boolean sCanAcceptPush = null;
    
    public static void onApplicationCreate(Context context) {
        if (sApplicationContext == null) {
            sApplicationContext = context.getApplicationContext();
        }
    }
    
    public static void onApplicationTerminate() {
        sApplicationContext = null;
        sContentResolver = null;
        sPackageName = null;
        sScreenDensity = null;
        sTextScale = null;
        sExternalStorageDirectory = null;
        sBestStorageDirectory = null;
        sLayoutInflater = null;
        sCanAcceptPush = null;
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
            sBestStorageDirectory = new File(StorageTricks.findWritableDirectoryWithMostFreeSpace(getContext(), includeAppDirectory), String.format(Locale.US, SDCARD_PATH_FORMAT, getPackageName()));
        }
        if (!sBestStorageDirectory.exists()) {
            sBestStorageDirectory.mkdirs();
        }
        return sBestStorageDirectory;
    }
    
    public static LayoutInflater getLayoutInflater() {
        if (sLayoutInflater == null) {
            sLayoutInflater = LayoutInflater.from(sApplicationContext);
        }
        return sLayoutInflater;
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

    public static int getResourceId(String name) {
        return getResources().getIdentifier(name, null, sApplicationContext.getPackageName());
    }

    public static Drawable getDrawable(int resource) {
        return getResources().getDrawable(resource);
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
    
    public static boolean canDeviceAcceptPushNotifications() {
        if (sCanAcceptPush == null) {
            if (Build.VERSION.SDK_INT < 8) {
                sCanAcceptPush = false;
            } else {
                sCanAcceptPush = isApplicationInstalled(ANDROID_MARKET_PACKAGE_NAME);
            }
        }
        return sCanAcceptPush;
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
    
}
