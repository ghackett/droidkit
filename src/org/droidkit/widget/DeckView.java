/*
 * THIS CLASS IS STILL IN DEVELOPMENT, DO NOT USE
 */

package org.droidkit.widget;

//org.droidkit.widget.HandyPagedView

import java.lang.ref.WeakReference;

import org.droidkit.DroidKit;
import org.droidkit.util.tricks.CLog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;


/*
* IMPORTANT: if you turn autoscrolling on, remember to turn it off in your activity's onPause
*/
public class DeckView extends RelativeLayout {
    
    public static final int DEFAULT_VISIBLE_SIDE_MARGIN_DP = 80;
    
    private RelativeLayout mTopContainer;
    
    private View mLeftView;
    private View mRightView;
    private View mTopView;

    private WeakReference<StoppableScrollView> mParentScrollview;
    private int mVisibleSideMarginPx;
    private Scroller mScroller;
    private int mTouchSlop;
    private int mMinVelocity;
    private int mMaxVelocity;
    private boolean mIsBeingDragged;
    private boolean mIsBeingScrolled;
    private float mLastMotionX;
    private VelocityTracker mVelocityTracker;
    private boolean mPreventInvalidate = false;

    public DeckView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDeckView();
    }

    public DeckView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDeckView();
    }

    public DeckView(Context context) {
        super(context);
        initDeckView();
    }
   
    private void initDeckView() {
        setWillNotCacheDrawing(true);
        setWillNotDraw(false);
        
        mVisibleSideMarginPx = DroidKit.getPixels(DEFAULT_VISIBLE_SIDE_MARGIN_DP);
        mIsBeingDragged = false;
        mIsBeingScrolled = false;
        mVelocityTracker = null;
        mParentScrollview = null;
        
        mScroller = new Scroller(getContext());
        
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();
        
        mTopContainer = new RelativeLayout(getContext());
    }
    
    public void setViews(View leftView, View rightView, View topView) {
        mLeftView = leftView;
        mRightView = rightView;
        mTopView = topView;
        updateLayout();
    }
    
    public void setVisibleSideMarginPx(int visibleSideMarginPx)
    {
        mVisibleSideMarginPx = visibleSideMarginPx;
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    updateLayout();
                }
            });
        }
    }
    
    private void updateLayout() {
        if (getWidth() <= 0 || mLeftView == null || mRightView == null || mTopView == null) {
            return;
        }
        removeAllViews();
        
        RelativeLayout.LayoutParams leftViewLayoutParams = new LayoutParams(getWidth() - mVisibleSideMarginPx, getHeight());
        mLeftView.setLayoutParams(leftViewLayoutParams);
        
        RelativeLayout.LayoutParams rightViewLayoutParams = new LayoutParams(getWidth() - mVisibleSideMarginPx, getHeight());
        rightViewLayoutParams.addRule(ALIGN_PARENT_RIGHT);
        mRightView.setLayoutParams(rightViewLayoutParams);
        
        int topContainerWidth = (getWidth() * 3) - (mVisibleSideMarginPx * 2);
        RelativeLayout.LayoutParams topContainerLayoutParams = new LayoutParams(topContainerWidth, getHeight());
        topContainerLayoutParams.leftMargin = -((topContainerWidth - getWidth())/2);
        mTopContainer.setLayoutParams(topContainerLayoutParams);
        mTopContainer.setBackgroundColor(Color.TRANSPARENT);
        mTopContainer.scrollTo(0, 0);
        
        RelativeLayout.LayoutParams topViewLayoutParams = new RelativeLayout.LayoutParams(getWidth(), getHeight());
//        topViewLayoutParams.addRule(CENTER_HORIZONTAL);
        topViewLayoutParams.leftMargin = getWidth() - mVisibleSideMarginPx;
        mTopView.setLayoutParams(topViewLayoutParams);
        
        addView(mLeftView);
        addView(mRightView);
        mTopContainer.addView(mTopView);
        addView(mTopContainer);
    }
    
    public void showLeft() {
        mRightView.setVisibility(View.INVISIBLE);
        mLeftView.setVisibility(View.VISIBLE);
        mTopContainer.scrollTo(getMinScrollX(), 0);
    }
    
    public void showRight() {
        mRightView.setVisibility(View.VISIBLE);
        mLeftView.setVisibility(View.INVISIBLE);
        mTopContainer.scrollTo(getMaxScrollX(), 0);

    }
    
    public void showTop() {
        mTopContainer.scrollTo(0, 0);
    }
    
    private boolean isOkToScroll(MotionEvent ev)
    {
        if (mIsBeingDragged || mIsBeingScrolled)
            return true;
        
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
            return true;
        
        float x = ev.getX();
        int scrollX = mTopContainer.getScrollX();
        
        CLog.e("Event X: " + x + " Scroll X: " + scrollX);
        
        if (scrollX == 0)
            return true;
        else if (scrollX < 0)
            return x > Math.abs(scrollX);
        else
            return x < getWidth()-scrollX;
        
//        CLog.e("Event X: " + x + " Scroll X: " + scrollX);
//        return true;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        
        if (!isOkToScroll(ev))
            return false;
        
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true;
        }
        
        switch(action & MotionEvent.ACTION_MASK) {
        
        case MotionEvent.ACTION_MOVE: {
            final float x = ev.getX();
            final int dx = (int)Math.abs(x - mLastMotionX);
            if (dx > mTouchSlop) {
                mIsBeingDragged = true;
                mLastMotionX = x;
                setParentScrollingAllowed(false);
            }
            break;
        }
        
        case MotionEvent.ACTION_DOWN: { 
            final float x = ev.getX();
            mLastMotionX = x;
            mIsBeingDragged = !mScroller.isFinished();
            break;
        }
        
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            mIsBeingDragged = false;
            setParentScrollingAllowed(true);
            break;
        }
        
        return mIsBeingDragged;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isOkToScroll(event))
            return false;
        
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        
        final int action = event.getAction();
        
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX();
                mIsBeingDragged = true;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastMotionX = x;
                break;
            }
            
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX();
                final int deltaX = (int) (mLastMotionX - x);
                mLastMotionX = x;
                scrollBy(deltaX, true);
                break;
            }
            
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                
                int initialVelocity = (int) velocityTracker.getXVelocity();
                
                mIsBeingDragged = false;
                if (Math.abs(initialVelocity) > mMinVelocity) {
                    fling(-initialVelocity);
                } else {
                    finishScroll();
                }
                
                
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                
                setParentScrollingAllowed(true);
                
                break;
            }
        }
        return true;

    }
    
    private void finishScroll() {
        int scrollX = mTopContainer.getScrollX();
        int maxX = getMaxScrollX();
        int minX = getMinScrollX();
        if (scrollX == 0 || scrollX == maxX || scrollX == minX) {
            mIsBeingDragged = false;
            mIsBeingScrolled = false;
        } else {
            if (scrollX < minX/2)
                smoothScrollTo(minX);
            else if (scrollX > maxX/2)
                smoothScrollTo(maxX);
            else
                smoothScrollTo(0);
        }
    }
    
    private void setParentScrollingAllowed(boolean allowed) {
        if (mParentScrollview != null) {
            StoppableScrollView ss = mParentScrollview.get();
            if (ss != null) {
                if (allowed)
                    ss.allowScrolling();
                else 
                    ss.stopScrolling();
            }
        }
    }
    
    public void setParentScrollView(StoppableScrollView parentScrollView) {
        mParentScrollview = new WeakReference<StoppableScrollView>(parentScrollView);
    }
    
    public void scrollTo(int scrollX, boolean invalidate) {
        if (!invalidate) {
            preventInvalidate();
        }
        
        int maxX = getMaxScrollX();
        int minX = getMinScrollX();
        
        if (scrollX < minX)
            scrollX = minX;
        if (scrollX > maxX)
            scrollX = maxX;
        
        if (scrollX < 0) {
            mLeftView.setVisibility(VISIBLE);
            mRightView.setVisibility(INVISIBLE);
        } else if (scrollX > 0) {
            mLeftView.setVisibility(INVISIBLE);
            mRightView.setVisibility(VISIBLE);
        }
        
        mTopContainer.scrollTo(scrollX, 0);
        allowInvalidate();
    }
    
    public void scrollBy(int dx, boolean invalidate) {
        scrollTo(mTopContainer.getScrollX()+dx, invalidate);
    }
    
    public void smoothScrollTo(int x) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        mScroller.startScroll(mTopContainer.getScrollX(), 0, x - mTopContainer.getScrollX(), 0);
        invalidate();
    }
    
    public void smoothScrollTo(int x, int duration) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        mScroller.startScroll(mTopContainer.getScrollX(), 0, x - mTopContainer.getScrollX(), 0, duration);
        invalidate();
    }
    
    public void fling(int initVelocity) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        
        int scrollX = mTopContainer.getScrollX();
        int maxX = getMaxScrollX();
        int minX = getMinScrollX();
        
        if (scrollX > 0)
            minX = 0;
        if (scrollX < 0)
            maxX = 0;
        
        mScroller.fling(scrollX, 0, initVelocity, 0, minX, maxX, 0, 0);
        invalidate();
    }
    
    private int getMaxScrollX() {
        return (getWidth() - mVisibleSideMarginPx);
    }
    
    private int getMinScrollX() {
        return -(getWidth() - mVisibleSideMarginPx);
    }
    
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mIsBeingScrolled = true;
            boolean finishScroll = false;
            int curX = mScroller.getCurrX();
            if (curX > getMaxScrollX()) {
                curX = getMaxScrollX();
                finishScroll = true;
            }
            if (curX < getMinScrollX()) {
                curX = getMinScrollX();
                finishScroll = true;
            }
            
            scrollTo(curX, false);
            postInvalidate();
            
            if (finishScroll) {
                mScroller.abortAnimation();
                finishScroll();
            }
        } else if (mIsBeingScrolled) {
            finishScroll();
        }
    }
    
    @Override
    public void invalidate() {
        if (!mPreventInvalidate)
            super.invalidate();
    }

    @Override
    public void invalidate(int l, int t, int r, int b) {
        if (!mPreventInvalidate)
            super.invalidate(l, t, r, b);
    }

    @Override
    public void invalidate(Rect dirty) {
        if (!mPreventInvalidate)
            super.invalidate(dirty);
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        if (!mPreventInvalidate)
            super.invalidateDrawable(drawable);
    }
    
    public void preventInvalidate() {
        mPreventInvalidate = true;
    }
    
    public void allowInvalidate() {
        mPreventInvalidate = false;
    }
    
    
}

