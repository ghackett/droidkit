package org.droidkit.image;

import java.io.File;
import java.io.IOException;

import org.droidkit.DroidKit;

import android.os.Environment;
import android.util.Log;

public class ImageCache {
    protected static final String TAG = "DroidKit";

//    protected static Context appContext = null;
//    public static void setContext(Context context) {
//        if (appContext == null)
//            appContext = context;
//    }

    protected static String cachePath = null;
    protected static boolean cachePathHasBeenVerified = false;

    public static String cachePath() {
        if (cachePath == null) {
            setCachePath(defaultCachePath());
        }
        if (!cachePathHasBeenVerified) {
            try {
                File nomediaFile = new File(cachePath + ".nomedia");
                if (!nomediaFile.exists()) {
                    new File(cachePath).mkdirs();
                    nomediaFile.createNewFile();
                }
                cachePathHasBeenVerified = true;
            } catch (IOException e) {
                Log.e(TAG, "Error creating image cache directories", e);
            }
            cachePathHasBeenVerified = true;
        }
        return cachePath;
    }
    public static void setCachePath(String path) {
        if (path.endsWith("/"))
            cachePath = path;
        else
            cachePath = path + "/";
        
        cachePathHasBeenVerified = false;
    }
    public static String defaultCachePath() {
        return new File(DroidKit.getExternalStorageDirectory(), "images").getAbsolutePath();
    }
    public static boolean cacheEnabled() {
        return cachePathHasBeenVerified && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    
    public static void touch(File cacheFile) {
        cacheFile.setLastModified(System.currentTimeMillis()); // Touch file for cache cleanup purposes
    }
}
