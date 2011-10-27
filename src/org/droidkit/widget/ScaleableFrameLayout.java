package org.droidkit.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ScaleableFrameLayout extends FrameLayout {
    
    private float mScale;

    public ScaleableFrameLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        initScaleableFrameLayout();
    }

    public ScaleableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScaleableFrameLayout();
    }

    public ScaleableFrameLayout(Context context) {
        super(context);
        initScaleableFrameLayout();
    }
    
    private void initScaleableFrameLayout() {
        setWillNotCacheDrawing(true);
        setWillNotDraw(false);
        
        mScale = 1.0f;
    }
    
    public void setScale(float scale) {
        mScale = scale;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        if (mScale != 1.0f)
            canvas.scale(mScale, mScale, 0, getHeight()/2);
    }
    
    

}
