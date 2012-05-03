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
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;


/*
* IMPORTANT: if you turn autoscrolling on, remember to turn it off in your activity's onPause
*/
public class DeckView extends RelativeLayout implements StoppableScrollView {
    
    public static final int DEFAULT_VISIBLE_SIDE_MARGIN_DP = 80;
    
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            // _o(t) = t * t * ((tension + 1) * t + tension)
            // o(t) = _o(t - 1) + 1
            t -= 1.0f;
            return t * t * t + 1.0f;
        }
    };

    
    private RelativeLayout mTopContainer;
    
    private View mLeftView;
    private View mRightView;
    private View mTopView;

    private WeakReference<StoppableScrollView> mTopScrollView;
    private int mVisibleSideMarginPx;
    private Scroller mScroller;
    private int mTouchSlop;
    private int mMinVelocity;
    private int mMaxVelocity;
    private boolean mIsBeingDragged;
    private boolean mIsBeingScrolled;
    private float mLastMotionX;
    private float mLastMotionY;
    private VelocityTracker mVelocityTracker;
    private boolean mPreventInvalidate = false;
    private int mMinScrollX;
    private int mMaxScrollX;
    private int mCenterScrollX;
    private Integer mScrollXOnDown = null;
    private Integer mTouchPointOnDown = null;
    
    private FrameLayout mLeftShadow = null;
    private FrameLayout mRightShadow = null;
    
    private boolean mScrollingDisabled = false;

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
        mTopScrollView = null;
        
        mScroller = new Scroller(getContext(), sInterpolator);
        
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
    
    public void setVisibleSideMarginPx(int visibleSideMarginPx) {
        mVisibleSideMarginPx = visibleSideMarginPx;
        updateLayout();
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
        resetScrollBarriers();
        
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
    
    private boolean isOkToScroll(MotionEvent ev) {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        
        if (mScrollingDisabled) {
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
                allowScrolling();
            return false;
        }
        
        if (mIsBeingDragged || mIsBeingScrolled)
            return true;
        
        
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
        CLog.e("onInterceptTouchEvent");
        
        if (!isOkToScroll(ev))
            return false;
        
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true;
        }
        
        switch(action & MotionEvent.ACTION_MASK) {
        
        case MotionEvent.ACTION_MOVE: {
            final float x = ev.getX();
            final float y = ev.getY();
            final int dx = (int)Math.abs(x - mLastMotionX);
            final int dy = (int)Math.abs(y - mLastMotionY);
            
            if (dy > mTouchSlop) {
                stopScrolling();
            }
            
            boolean startDrag = dx > mTouchSlop;
//            if (mScrollXOnDown != null && mTouchPointOnDown != null && mScrollXOnDown == getCenterScrollX()) {
//                if (mTouchPointOnDown > mVisibleSideMarginPx && mTouchPointOnDown < (getWidth()-mVisibleSideMarginPx))
//                    startDrag = false;
//            }
            if (startDrag) {
                mIsBeingDragged = true;
                mLastMotionX = x;
                setTopScrollingAllowed(false);
                mScrollXOnDown = mTopContainer.getScrollX();
            }
            break;
        }
        
        case MotionEvent.ACTION_DOWN: { 
            mScrollXOnDown = mTopContainer.getScrollX();
            mTouchPointOnDown = (int) ev.getX();
            
            final float x = ev.getX();
            final float y = ev.getY();
            mLastMotionX = x;
            mLastMotionY = y;
            mIsBeingDragged = !mScroller.isFinished();
            break;
        }
        
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            mIsBeingDragged = false;
            setTopScrollingAllowed(true);
            mScrollXOnDown = null;
            mTouchPointOnDown = null;
            break;
        }
        
        return mIsBeingDragged;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        CLog.e("onTouchEvent");
        
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
                if (mScrollXOnDown == null)
                    mScrollXOnDown = mTopContainer.getScrollX();
                
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    mIsBeingDragged = true;
                    mIsBeingScrolled = false;
                    setTopScrollingAllowed(false);
                }
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
                
                setTopScrollingAllowed(true);
                mScrollXOnDown = null;
                mTouchPointOnDown = null;
                
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
            mScrollXOnDown = null;
            setTopScrollingAllowed(true);
        } else {
            if (scrollX < (centerX + minX)/2)
                smoothScrollTo(minX);
            else if (scrollX > (centerX + maxX)/2)
                smoothScrollTo(maxX);
            else
                smoothScrollTo(centerX);
        }
    }
    
    private void setTopScrollingAllowed(boolean allowed) {
        if (mTopScrollView != null) {
            StoppableScrollView ss = mTopScrollView.get();
            if (ss != null) {
                if (allowed)
                    ss.allowScrolling();
                else 
                    ss.stopScrolling();
            }
        }
    }
    
    public void setTopScrollView(StoppableScrollView parentScrollView) {
        mTopScrollView = new WeakReference<StoppableScrollView>(parentScrollView);
    }
    
    protected void scrollTo(int scrollX, boolean invalidate) {
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
    
    protected void scrollBy(int dx, boolean invalidate) {
        scrollTo(mTopContainer.getScrollX()+dx, invalidate);
    }
    
    protected void smoothScrollTo(int x) {
        smoothScrollTo(x, 250);
    }
    
    protected void smoothScrollTo(int x, int duration) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        mScroller.startScroll(mTopContainer.getScrollX(), 0, x - mTopContainer.getScrollX(), 0, duration);
        invalidate();
    }
    
    protected void fling(int initVelocity) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        
        int scrollX = mTopContainer.getScrollX();
        int maxX = getMaxScrollX();
        int minX = getMinScrollX();
        int centerX = getCenterScrollX();
        
        if (mScrollXOnDown != null) {
            if ((mScrollXOnDown > centerX && scrollX < centerX) 
                    || (mScrollXOnDown < centerX && scrollX > centerX)){
                finishScroll();
                return;
            }
        }
        
        if (scrollX > centerX)
            minX = centerX;
        if (scrollX < centerX)
            maxX = centerX;
        
        mScroller.fling(scrollX, 0, initVelocity, 0, minX, maxX, 0, 0);
        invalidate();
    }
    
    private void resetScrollBarriers() {
        if (getWidth() <= 0)
            return;
        mMaxScrollX = 0;
        mMinScrollX = -((getWidth() * 2) - (mVisibleSideMarginPx * 2));
        mCenterScrollX = -(getWidth() - mVisibleSideMarginPx);
    }
    
    private int getMaxScrollX() {
        return mMaxScrollX;
    }
    
    private int getMinScrollX() {
        return mMinScrollX;
    }
    
    private int getCenterScrollX() {
        return mCenterScrollX;
    }
    
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mIsBeingScrolled = true;
            boolean finishScroll = false;
            int curX = mScroller.getCurrX();
            if (curX == mScroller.getFinalX())
                finishScroll = true;
            
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

    @Override
    public void stopScrolling() {
        mScrollingDisabled = true;
    }

    @Override
    public void allowScrolling() {
        mScrollingDisabled = false;
    }

    @Override
    public boolean isScrollingAllowed() {
        return !mScrollingDisabled;
    }
    
    
}

