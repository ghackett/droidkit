/*
 * THIS CLASS IS STILL IN DEVELOPMENT, DO NOT USE
 */

package org.droidkit.widget;

//org.droidkit.widget.HandyPagedView

import java.lang.ref.WeakReference;

import org.droidkit.DroidKit;
import org.droidkit.R;
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
import android.view.ViewGroup;
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
    
    private FrameLayout mLeftShadow = null;
    private FrameLayout mRightShadow = null;

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
    
    public void setViews(View leftView, View rightView, View topView, boolean useShadows) {
        mLeftView = leftView;
        mRightView = rightView;
        mTopView = topView;
        
        if (useShadows) {
            if (mLeftShadow == null) {
                mLeftShadow = new FrameLayout(getContext());
                mLeftShadow.setBackgroundResource(R.drawable.bg_shadow_left);
            }
            if (mRightShadow == null) {
                mRightShadow = new FrameLayout(getContext());
                mRightShadow.setBackgroundResource(R.drawable.bg_shadow_right);
            }
        } else {
            if (mLeftShadow != null) {
                if (mLeftShadow.getParent() != null)
                    ((ViewGroup)mLeftShadow.getParent()).removeView(mLeftShadow);
                mLeftShadow = null;
            }
            if (mRightShadow != null) {
                if (mRightShadow.getParent() != null)
                    ((ViewGroup)mRightShadow.getParent()).removeView(mRightShadow);
                mRightShadow = null;
            }
        }
        
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
        
        mTopContainer.removeAllViews();
        removeAllViews();
        
        RelativeLayout.LayoutParams leftViewLayoutParams = new LayoutParams(getWidth() - mVisibleSideMarginPx, getHeight());
        mLeftView.setLayoutParams(leftViewLayoutParams);
        
        RelativeLayout.LayoutParams rightViewLayoutParams = new LayoutParams(getWidth() - mVisibleSideMarginPx, getHeight());
        rightViewLayoutParams.addRule(ALIGN_PARENT_RIGHT);
        mRightView.setLayoutParams(rightViewLayoutParams);
        
        int topContainerWidth = (getWidth() * 3) - (mVisibleSideMarginPx * 2);
        RelativeLayout.LayoutParams topContainerLayoutParams = new LayoutParams(topContainerWidth, getHeight());
        topContainerLayoutParams.leftMargin = (-topContainerWidth) + getWidth();
        mTopContainer.setLayoutParams(topContainerLayoutParams);
        mTopContainer.setBackgroundColor(Color.TRANSPARENT);
        mTopContainer.scrollTo(getCenterScrollX(), 0);
        
        RelativeLayout.LayoutParams topViewLayoutParams = new RelativeLayout.LayoutParams(getWidth(), getHeight());
        topViewLayoutParams.leftMargin = getWidth() - mVisibleSideMarginPx;
        mTopView.setLayoutParams(topViewLayoutParams);
        
        mTopContainer.addView(mTopView);
        
        if (mLeftShadow != null && mRightShadow != null) {
            
            RelativeLayout.LayoutParams leftParams = new LayoutParams(getWidth() - mVisibleSideMarginPx, LayoutParams.MATCH_PARENT);
            RelativeLayout.LayoutParams rightParams = new LayoutParams(getWidth() - mVisibleSideMarginPx, LayoutParams.MATCH_PARENT);
            rightParams.leftMargin = (getWidth()*2 - mVisibleSideMarginPx);
            
            mTopContainer.addView(mLeftShadow, leftParams);
            mTopContainer.addView(mRightShadow, rightParams);
        }
        
        addView(mLeftView);
        addView(mRightView);
        addView(mTopContainer);
    }
    
    public void showLeft(boolean animated) {
        if (animated)
            smoothScrollTo(getMinScrollX());
        else
            scrollTo(getMinScrollX(), true);
    }
    
    public void showRight(boolean animated) {
        if (animated)
            smoothScrollTo(getMaxScrollX());
        else
            scrollTo(getMaxScrollX(), true);
    }
    
    public void showTop(boolean animated) {
        if (animated)
            smoothScrollTo(getCenterScrollX());
        else
            scrollTo(getCenterScrollX(), true);
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
        int centerX = getCenterScrollX();
        int trueX = scrollX + (int)x;
        
        if (scrollX == centerX)
            return true;
        else if (scrollX < centerX)
            return trueX > getCenterScrollX();
        else
            return trueX < (getCenterScrollX() + getWidth());
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
        int centerX = getCenterScrollX();
        if (scrollX == centerX || scrollX == maxX || scrollX == minX) {
            mIsBeingDragged = false;
            mIsBeingScrolled = false;
        } else {
            if (scrollX < (centerX + minX)/2)
                smoothScrollTo(minX);
            else if (scrollX > (centerX + maxX)/2)
                smoothScrollTo(maxX);
            else
                smoothScrollTo(centerX);
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
        int centerX = getCenterScrollX();
        
        if (scrollX < minX)
            scrollX = minX;
        if (scrollX > maxX)
            scrollX = maxX;
        
        if (scrollX < centerX) {
            mLeftView.setVisibility(VISIBLE);
            mRightView.setVisibility(INVISIBLE);
        } else if (scrollX > centerX) {
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
        int centerX = getCenterScrollX();
        
        if (scrollX > centerX)
            minX = centerX;
        if (scrollX < centerX)
            maxX = centerX;
        
        mScroller.fling(scrollX, 0, initVelocity, 0, minX, maxX, 0, 0);
        invalidate();
    }
    
    private int getMaxScrollX() {
        return 0;
//        return (getWidth() - mVisibleSideMarginPx);
    }
    
    private int getMinScrollX() {
        return -((getWidth() * 2) - (mVisibleSideMarginPx * 2));
//        return -(getWidth() - mVisibleSideMarginPx);
    }
    
    private int getCenterScrollX() {
        return -(getWidth() - mVisibleSideMarginPx);
//        return 0;
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

