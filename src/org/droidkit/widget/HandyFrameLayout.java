package org.droidkit.widget;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class HandyFrameLayout extends FrameLayout {
    
    public interface OnSizeChangedListener {
        public void onSizeChanged(HandyFrameLayout frameLayout, int w, int h, int oldw, int oldh);
    }
    
    private WeakReference<OnSizeChangedListener> mSizeListener;

    public HandyFrameLayout(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public HandyFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public HandyFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public void setOnSizeChangedListener(OnSizeChangedListener listener) {
        mSizeListener = new WeakReference<HandyFrameLayout.OnSizeChangedListener>(listener);
    }
    
    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mSizeListener != null) {
            OnSizeChangedListener listener = mSizeListener.get();
            if (listener != null)
                listener.onSizeChanged(this, w, h, oldw, oldh);
        }
    }
}
