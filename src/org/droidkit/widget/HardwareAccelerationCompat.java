package org.droidkit.widget;

import android.graphics.Canvas;
import android.view.View;

public class HardwareAccelerationCompat {
    
    public static boolean isViewHardwareAccelerated(View v) {
        return v.isHardwareAccelerated();
    }
    
    public static boolean isCanvasHardwareAccelerated(Canvas c) {
        return c.isHardwareAccelerated();
    }
    
    public static int getCanvasMaxDimension(Canvas c) {
        return Math.max(c.getMaximumBitmapHeight(), c.getMaximumBitmapWidth());
    }

}
