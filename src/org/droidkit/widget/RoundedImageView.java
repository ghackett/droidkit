/*
 * Borrowed from http://wiresareobsolete.com/2011/08/quick-rounded-corners/
 */
package org.droidkit.widget;

import org.droidkit.DroidKit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundedImageView extends ImageView {
    
    private float[] mRadii = new float[] {
            (float)DroidKit.getPixels(10), (float)DroidKit.getPixels(10),
            (float)DroidKit.getPixels(10), (float)DroidKit.getPixels(10),
            (float)DroidKit.getPixels(10), (float)DroidKit.getPixels(10),
            (float)DroidKit.getPixels(10), (float)DroidKit.getPixels(10)
    };

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setCornerRadius(int radiusDip) {
        float radius = DroidKit.getPixels(radiusDip);
        for (int i = 0; i<mRadii.length; i++)
            mRadii[i] = radius;
    }
    
    public void setCornerRadius(int topLeftDip, int topRightDip, int bottomRightDip, int bottomLeftDip) {
        mRadii[0] = DroidKit.getPixels(topLeftDip);
        mRadii[1] = mRadii[0];
        
        mRadii[2] = DroidKit.getPixels(topRightDip);
        mRadii[3] = mRadii[2];
        
        mRadii[4] = DroidKit.getPixels(bottomRightDip);
        mRadii[5] = mRadii[4];
        
        mRadii[6] = DroidKit.getPixels(bottomLeftDip);
        mRadii[7] = mRadii[6];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!DroidKit.isHoneycomb() || !HardwareAccelerationCompat.isCanvasHardwareAccelerated(canvas)) {
            Path clipPath = new Path();
            RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
            clipPath.addRoundRect(rect, mRadii, Path.Direction.CW);
            canvas.clipPath(clipPath);
        }

        super.onDraw(canvas);
    }
}
