To get the most use out of DroidKit, you must manage its lifecycle inside your android.app.Application singleton.

Example Application Class:

package com.my.android.app;

import org.droidkit.DroidKit;

public class MyApp extends android.app.Application {
	
	@Override
    public void onCreate() {
        DroidKit.onApplicationCreate(this);
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        DroidKit.onApplicationTerminate();
        super.onTerminate();
    }
}

Then set your Application's class name in your AndroidManifest.xml by adding 

android:name="com.my.android.app.MyApp"

to the <application> tag.