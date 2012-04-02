package org.droidkit.widget;

//org.droidkit.widget.HandyScrollView

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

public class HandyScrollView extends ScrollView implements StoppableScrollView {
	
	public interface OnSizeChangedListener {
		public void onSizeChanged(HandyScrollView scrollView, int w, int h, int oldw, int oldh);
	}
	
	private int mFadingEdgeColor = -1;
	private WeakReference<OnSizeChangedListener> mSizeListener = null;
	private boolean mPreventScrolling = false;
	private ArrayList<WeakReference<StoppableScrollView>> mStoppableScrollViews = new ArrayList<WeakReference<StoppableScrollView>>();
	private int mTouchSlop;
	private float mLastMotionY;

	public HandyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initHandyScrollView();
	}

	public HandyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHandyScrollView();
	}

	public HandyScrollView(Context context) {
		super(context);
		initHandyScrollView();
	}
	
	private void initHandyScrollView() {
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
	}
	
	public void addStoppableScrollView(StoppableScrollView stoppableScrollView) {
	    if (stoppableScrollView != null) {
	        mStoppableScrollViews.add(new WeakReference<StoppableScrollView>(stoppableScrollView));
	    }
	}
	
	private void setStoppableScrollingAllowed(boolean allowed) {
	    for (WeakReference<StoppableScrollView> ref : mStoppableScrollViews) {
	        StoppableScrollView sv = ref.get();
	        if (sv != null) {
	            if (allowed)
	                sv.allowScrolling();
	            else
	                sv.stopScrolling();
	        }
	    }
	}
	
	public void setFadingEdgeColor(int color) {
		mFadingEdgeColor = color;
	}
	
	@Override
	public int getSolidColor() {
		if (mFadingEdgeColor == -1)
			return super.getSolidColor();
		return mFadingEdgeColor;
	}
	
	public void setOnSizeChangedListener(OnSizeChangedListener listener) {
		mSizeListener = new WeakReference<HandyScrollView.OnSizeChangedListener>(listener);
	}
	
	public void stopScrolling() {
		mPreventScrolling = true;
	}
	
	public void allowScrolling() {
		mPreventScrolling = false;
	}
	
	public boolean isScrollingAllowed() {
		return !mPreventScrolling;
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

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mPreventScrolling)
			return false;
		
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = ev.getY();
                break;    
            case MotionEvent.ACTION_MOVE:
                float y = ev.getY();
                int dy = (int)Math.abs(y - mLastMotionY);
                if (dy > mTouchSlop) {
                    setStoppableScrollingAllowed(false);
                    mLastMotionY = y;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setStoppableScrollingAllowed(true);
                break;
        }
	        
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mPreventScrolling)
			return false;
		
		int action = ev.getAction() & MotionEvent.ACTION_MASK;
		
		switch(action) {
		    case MotionEvent.ACTION_DOWN:
		        mLastMotionY = ev.getY();
		        break;    
		    case MotionEvent.ACTION_MOVE:
                float y = ev.getY();
                int dy = (int)Math.abs(y - mLastMotionY);
                if (dy > mTouchSlop) {
                    setStoppableScrollingAllowed(false);
                    mLastMotionY = y;
                }
		        break;
		    case MotionEvent.ACTION_CANCEL:
		    case MotionEvent.ACTION_UP:
		        setStoppableScrollingAllowed(true);
		        break;
		}
		
		return super.onTouchEvent(ev);
	}

	
	

}
