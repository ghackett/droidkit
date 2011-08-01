/*
 * Copyright 2010 Mike Novak <michael.novakjr@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.droidkit.util.tricks;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

/**
 * A wrapper for the Android Log class that throttles the types of messages
 * pushed to the console. The goal is to only have error messages print by
 * default but have the ability to turn on more verbose logging in the event
 * something needs to be debugged.
 * 
 * @version 1
 * @since 1
 * @author mrn
 */
public class Log {
    
    private static String DEFAULT_TAG = "DroidKit";

    /**
     * Prints a debug message to the Android console log. The message will only
     * print if the log level set on the device is at least at DEBUG for the
     * provided tag.
     * 
     * @param message The message to print to the log.
     * @since 1
     */
    public static void d(String tag, String message) {
        if (android.util.Log.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.d(tag, message);
        }
    }

    /**
     * Prints an info message to the Android console log. The message will only
     * print if the log level set on the device is at least at INFO for the
     * provided tag.
     * 
     * @param message The message to print to the log.
     * @since 1
     */
    public static void i(String tag, String message) {
        if (android.util.Log.isLoggable(tag, android.util.Log.INFO)) {
            android.util.Log.d(tag, message);
        }
    }

    /**
     * Prints a warning message to the Android console log. The message will
     * only print if the log level set on the device is at least at WARN for the
     * provided tag.
     * 
     * @param message The message to print to the log.
     * @since 1
     */
    public static void w(String tag, String message) {
        if (android.util.Log.isLoggable(tag, android.util.Log.WARN)) {
            android.util.Log.w(tag, message);
        }
    }

    /**
     * Prints an error message to the Android console log. The message will only
     * print if the log level set on the device is at least at ERROR for the
     * provided tag.
     * 
     * @param message The message to print to the log.
     * @since 1
     */
    public static void e(String tag, String message) {
        if (android.util.Log.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(tag, message);
        }
    }

    /**
     * Prints a verbose message to the Android console log. The message will
     * only print if the log level set on the device is at least VERBOSE for the
     * provided tag..
     * 
     * @param message The message to print to the log.
     * @since 1
     */
    public static void v(String tag, String message) {
        if (android.util.Log.isLoggable(tag, android.util.Log.VERBOSE)) {
            android.util.Log.v(tag, message);
        }
    }
    

    
    
    
    
    public static void d(String tag, String message, Throwable t) {
        if (android.util.Log.isLoggable(tag, android.util.Log.DEBUG)) {
            android.util.Log.d(tag, message, t);
        }
    }


    public static void i(String tag, String message, Throwable t) {
        if (android.util.Log.isLoggable(tag, android.util.Log.INFO)) {
            android.util.Log.d(tag, message, t);
        }
    }


    public static void w(String tag, String message, Throwable t) {
        if (android.util.Log.isLoggable(tag, android.util.Log.WARN)) {
            android.util.Log.w(tag, message, t);
        }
    }
    
    public static void e(String tag, String message, Throwable t) {
        if (android.util.Log.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(tag, message, t);
        }
    }
    
    public static void e(String tag, String message, Intent i) {
        if (android.util.Log.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(tag, message + "\n" + describeIntent(i));
        }
    }
    
    public static void e(String tag, String message, Cursor c) {
        if (android.util.Log.isLoggable(tag, android.util.Log.ERROR)) {
            android.util.Log.e(tag, message);
            if (c != null && !c.isClosed() && !c.isAfterLast() && !c.isBeforeFirst()) {
                int colCount = c.getColumnCount();
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i<colCount; i++) {
                    if (i!=0)
                        builder.append(", ");
                    builder.append(c.getColumnName(i) + " = " + c.getString(i));
                }
                Log.e(tag, builder.toString());
            }
        }
    }

    public static void v(String tag, String message, Throwable t) {
        if (android.util.Log.isLoggable(tag, android.util.Log.VERBOSE)) {
            android.util.Log.v(tag, message, t);
        }
    }
    
    public static void d(String msg) {
        d(DEFAULT_TAG, msg);
    }
    public static void i(String msg) {
        i(DEFAULT_TAG, msg);
    }
    public static void w(String msg) {
        w(DEFAULT_TAG, msg);
    }
    public static void e(String msg) {
        e(DEFAULT_TAG, msg);
    }
    public static void e(String msg, Throwable t) {
        e(DEFAULT_TAG, msg, t);
    }
    public static void e(String msg, Intent i) {
        e(DEFAULT_TAG, msg, i);
    }
    
    private static String describeIntent(Intent i) {
        if (i == null)
            return "Intent is null";
        
        StringBuilder builder = new StringBuilder("Intent\n");
        
        builder.append("ACTION: ");
        builder.append(i.getAction());
        builder.append("\n");
        
        builder.append("DATA: ");
        builder.append(i.getDataString());
        builder.append("\n");
        
        builder.append("DATA TYPE: ");
        builder.append(i.getType());
        builder.append("\n");
        
        builder.append("PACKAGE NAME: ");
        builder.append(i.getPackage());
        builder.append("\n");
        
        builder.append("COMPONENT NAME: ");
        if (i.getComponent() != null)
            builder.append(i.getComponent().toString());
        builder.append("\n");
        
        Bundle extras = i.getExtras();
        if (extras != null) {
            builder.append("EXTRAS:\n");
            for (String key : extras.keySet()) {
                builder.append(key + " = ");
                builder.append(String.valueOf(extras.get(key)));
                builder.append("\n");
            }
        }
        
        return builder.toString();
    }

}
