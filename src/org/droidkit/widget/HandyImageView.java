package org.droidkit.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class HandyImageView extends ImageView {

    public HandyImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initHandyImageView();
    }

    public HandyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHandyImageView();
    }

    public HandyImageView(Context context) {
        super(context);
        initHandyImageView();
    }
    
    private void initHandyImageView() {
        
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    

}
