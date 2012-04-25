package org.droidkit.util.tricks;

import org.droidkit.DroidKit;

import android.content.Intent;
import android.database.Cursor;

public class CLog {
    
    private static String LOG_TAG = "DroidKit";
    
    public static void setTag(String tag) {
        LOG_TAG = tag;
    }
    
    
    public static void d(String msg) {
        d(msg, null);
    }
    
    public static void d(String msg, Throwable t) {
        if (android.util.Log.isLoggable(LOG_TAG, android.util.Log.DEBUG)) {
            if (t == null)
                android.util.Log.d(LOG_TAG, msg);
            else
                android.util.Log.d(LOG_TAG, msg, t);
        }
    }
    
    
    
    
    
    public static void e(String msg) {
        e(msg, (Throwable)null);
    }
    
    public static void e(String msg, Throwable t) {
        if (android.util.Log.isLoggable(LOG_TAG, android.util.Log.ERROR)) {
            if (t == null)
                android.util.Log.e(LOG_TAG, msg);
            else
                android.util.Log.e(LOG_TAG, msg, t);
        }
    }
    
    public static void e(String msg, Intent i) {
        if (android.util.Log.isLoggable(LOG_TAG, android.util.Log.ERROR)) {
            android.util.Log.e(LOG_TAG, msg);
            if (i != null) {
                String[] intentDesc = Log.describeIntent(i).split("\n");
                for (int j = 0; j<intentDesc.length; j++) {
                    android.util.Log.e(LOG_TAG, intentDesc[j]);
                }
            }
        }
    }
    
    public static void e(String msg, Cursor c) {
        if (android.util.Log.isLoggable(LOG_TAG, android.util.Log.ERROR)) {
            android.util.Log.e(LOG_TAG, msg);
            if (c != null && !c.isClosed() && !c.isAfterLast() && !c.isBeforeFirst()) {
                int colCount = c.getColumnCount();
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i<colCount; i++) {
                    if (i!=0)
                        builder.append(", ");
                    builder.append(c.getColumnName(i) + " = " + c.getString(i));
                }
                android.util.Log.e(LOG_TAG, builder.toString());
            }
        }
    }
    
    
    
    
    
    
    public static void i(String msg) {
        d(msg, null);
    }
    
    public static void i(String msg, Throwable t) {
        if (android.util.Log.isLoggable(LOG_TAG, android.util.Log.INFO)) {
            if (t == null)
                android.util.Log.i(LOG_TAG, msg);
            else
                android.util.Log.i(LOG_TAG, msg, t);
        }
    }
    
    
    
    
    
    
    public static void v(String msg) {
        d(msg, null);
    }
    
    public static void v(String msg, Throwable t) {
        if (android.util.Log.isLoggable(LOG_TAG, android.util.Log.VERBOSE)) {
            if (t == null)
                android.util.Log.v(LOG_TAG, msg);
            else
                android.util.Log.v(LOG_TAG, msg, t);
        }
    }
    
    
    
    
    
    public static void w(String msg) {
        w(msg, null);
    }
    
    public static void w(Throwable t) {
        w(null, t);
    }
    
    public static void w(String msg, Throwable t) {
        if (android.util.Log.isLoggable(LOG_TAG, android.util.Log.WARN)) {
            if (t == null)
                android.util.Log.w(LOG_TAG, msg);
            else if (msg == null)
                android.util.Log.w(LOG_TAG, t);
            else
                android.util.Log.w(LOG_TAG, msg, t);
        }
    }
    
    
    
    
    
    
    
    public static void wtf(String msg) {
        w(msg, null);
    }
    
    public static void wtf(Throwable t) {
        w(null, t);
    }
    
    public static void wtf(String msg, Throwable t) {
        if (DroidKit.isFroyo()) {
            if (android.util.Log.isLoggable(LOG_TAG, android.util.Log.WARN)) {
                if (t == null)
                    android.util.Log.w(LOG_TAG, msg);
                else if (msg == null)
                    android.util.Log.w(LOG_TAG, t);
                else
                    android.util.Log.w(LOG_TAG, msg, t);
            }
        } else {
            w(msg, t);
        }
    }

}
