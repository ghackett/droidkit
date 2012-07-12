/*
 * Copyright (C) 2012 GroupMe, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.droidkit.widget;

//org.droidkit.widget.DeckView

import org.droidkit.DroidKit;
import org.droidkit.R;
import org.droidkit.util.tricks.CLog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

/**
 * 
 * @author Geoff Hackett
 *
 */
public class DeckView extends FrameLayout implements StoppableScrollView {
    
    public static final int DEFAULT_VISIBLE_SIDE_MARGIN_DP = 80;
    
    public static final int DECK_TOP = 0;
    public static final int DECK_LEFT = -1;
    public static final int DECK_RIGHT = 1;
    public static final int DECK_UNKNOWN = Integer.MAX_VALUE;
    
    public static final int MODE_0_SIDES = 0; //default
    public static final int MODE_1_SIDE = 1; //always show one of the panels in addition to the top view (top will never be hidden)
    public static final int MODE_2_SIDES = 2; //always show both of the panels (top will never be hidden and will never scroll left or right)
    
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            // _o(t) = t * t * ((tension + 1) * t + tension)
            // o(t) = _o(t - 1) + 1
            t -= 1.0f;
            return t * t * t + 1.0f;
        }
    };

    public interface OnDeckFocusChangedListener {
        public void onDeckFocusChanged(int newFocus);
        public void onVisibilityChanged(int visibleSide);
    }
    
    private FrameLayout mTopContainer;
    
    private View mLeftView;
    private View mRightView;
    private View mTopView;

//    private WeakReference<StoppableScrollView> mTopScrollView;
    private Scroller mScroller;
    private int mTouchSlop;
    private int mMinVelocity;
    private int mMaxVelocity;
    private boolean mIsBeingDragged;
    private boolean mIsBeingScrolled;
    private float mLastMotionX;
    private float mLastMotionY;
    private boolean mTouchMightBeTap = false;
    private VelocityTracker mVelocityTracker;
    private boolean mPreventInvalidate = false;
    private int mMinScrollX;
    private int mMaxScrollX;
    private int mCenterScrollX;
    private Integer mScrollXOnDown = null;
    private OnDeckFocusChangedListener mFocusChangedListener = null;
//    private Integer mTouchPointOnDown = null;
    
    private FrameLayout mLeftShadow = null;
    private FrameLayout mRightShadow = null;
    
    private boolean mScrollingDisabled = false;
    
    private int mCurrentDeckFocus = DECK_UNKNOWN;
    private int mDeckMode = MODE_0_SIDES;
    
    private int mVisibleSideMarginPx; //only used for MODE_0_SIDES
    private int mLeftPanelWidth; //only used for MODE_1_SIDE and MODE_2_SIDES
    private int mRightPanelWidth; //only used for MODE_2_SIDES

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
//        setWillNotCacheDrawing(false);
//        setWillNotDraw(true);
        
        mVisibleSideMarginPx = DroidKit.getPixels(DEFAULT_VISIBLE_SIDE_MARGIN_DP);
        mIsBeingDragged = false;
        mIsBeingScrolled = false;
        mVelocityTracker = null;
//        mTopScrollView = null;
        
        mScroller = new Scroller(getContext(), sInterpolator);
        
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();
        
        mTopContainer = new FrameLayout(getContext());
        
        addView(mTopContainer);
    }
    
    public void setOnDeckFocusChangedListener(OnDeckFocusChangedListener listener) {
        mFocusChangedListener = listener;
    }
    
    public void setViews(View leftView, View rightView, View topView, boolean useShadows) {
        
        if (mLeftView != null && mLeftView.getParent() != null)
            removeView(mLeftView);
        if (mRightView != null && mRightView.getParent() != null)
            removeView(mRightView);
        if (mTopView != null && mTopContainer.getParent() != null)
            mTopContainer.removeView(mTopView);
            
        
        mLeftView = leftView;
        mRightView = rightView;
        mTopView = topView;
        
        mTopContainer.addView(mTopView);
        addView(mLeftView, 0);
        addView(mRightView, 0);
        
        if (useShadows) {
            if (mLeftShadow == null) {
                mLeftShadow = new FrameLayout(getContext());
                mLeftShadow.setBackgroundResource(R.drawable.bg_shadow_left);
                mTopContainer.addView(mLeftShadow);
            }
            if (mRightShadow == null) {
                mRightShadow = new FrameLayout(getContext());
                mRightShadow.setBackgroundResource(R.drawable.bg_shadow_right);
                mTopContainer.addView(mRightShadow);
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
    
    public void setDeckMode0Sides(int visibleSideMarginPx) {
        mDeckMode = MODE_0_SIDES;
        mVisibleSideMarginPx = visibleSideMarginPx;
        updateLayout();
    }
    
    public void setDeckMode1Side(int panelWidthPx) {
        mDeckMode = MODE_1_SIDE;
        mLeftPanelWidth = panelWidthPx;
        mRightPanelWidth = panelWidthPx;
        updateLayout();
    }
    
    public void setDeckMode2Sides(int leftPanelWidthPx, int rightPanelWidthPx) {
        mDeckMode = MODE_2_SIDES;
        mLeftPanelWidth = leftPanelWidthPx;
        mRightPanelWidth = rightPanelWidthPx;
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            CLog.v("ON LAYOUT - CHANGED");
            getHandler().post(mUpdateLayoutRunnable);
        }
    }
    
    private Runnable mUpdateLayoutRunnable = new Runnable() {
        
        @Override
        public void run() {
            updateLayout();
        }
    };
    
    

    private void updateLayout() {
        if (getWidth() <= 0 || mLeftView == null || mRightView == null || mTopView == null) {

            return;
        }
        

        resetScrollBarriers();
        
        switch(mDeckMode) {
            case MODE_0_SIDES: {
                FrameLayout.LayoutParams leftViewLayoutParams = new LayoutParams(getWidth() - mVisibleSideMarginPx, getHeight());
                mLeftView.setLayoutParams(leftViewLayoutParams);
                
                FrameLayout.LayoutParams rightViewLayoutParams = new LayoutParams(getWidth() - mVisibleSideMarginPx, getHeight());
                rightViewLayoutParams.leftMargin = mVisibleSideMarginPx;
                rightViewLayoutParams.gravity = Gravity.TOP;
//                rightViewLayoutParams.addRule(ALIGN_PARENT_RIGHT);
                mRightView.setLayoutParams(rightViewLayoutParams);
                
                int topContainerWidth = (getWidth() * 3) - (mVisibleSideMarginPx * 2);
                FrameLayout.LayoutParams topContainerLayoutParams = new LayoutParams(topContainerWidth, getHeight());
                topContainerLayoutParams.gravity = Gravity.TOP;
                topContainerLayoutParams.leftMargin = (-topContainerWidth) + getWidth();
                mTopContainer.setLayoutParams(topContainerLayoutParams);
                mTopContainer.setBackgroundColor(Color.TRANSPARENT);
                

                
                FrameLayout.LayoutParams topViewLayoutParams = new FrameLayout.LayoutParams(getWidth(), getHeight());
                
                topViewLayoutParams.leftMargin = getWidth() - mVisibleSideMarginPx;
                topViewLayoutParams.gravity = Gravity.TOP;
                mTopView.setLayoutParams(topViewLayoutParams);
                
                
                if (mLeftShadow != null && mRightShadow != null) {
                    
                    FrameLayout.LayoutParams leftParams = new FrameLayout.LayoutParams(getWidth() - mVisibleSideMarginPx, LayoutParams.MATCH_PARENT);
                    FrameLayout.LayoutParams rightParams = new FrameLayout.LayoutParams(getWidth() - mVisibleSideMarginPx, LayoutParams.MATCH_PARENT);
                    rightParams.leftMargin = (getWidth()*2 - mVisibleSideMarginPx);
                    rightParams.gravity = Gravity.TOP;
                    
                    mLeftShadow.setLayoutParams(leftParams);
                    mRightShadow.setLayoutParams(rightParams);
                    
                }
                
                switch (mCurrentDeckFocus) {
                    case DECK_UNKNOWN:
                    case DECK_TOP:
                        mTopContainer.scrollTo(getCenterScrollX(), 0);
                        mLeftView.setVisibility(View.INVISIBLE);
                        mRightView.setVisibility(View.INVISIBLE);
                        break;
                    case DECK_LEFT:
                        mTopContainer.scrollTo(getMinScrollX(), 0);
                        mLeftView.setVisibility(View.VISIBLE);
                        mRightView.setVisibility(View.INVISIBLE);
                        break;
                    case DECK_RIGHT:
                        mTopContainer.scrollTo(getMaxScrollX(), 0);
                        mLeftView.setVisibility(View.INVISIBLE);
                        mRightView.setVisibility(View.VISIBLE);
                        break;
                }
                break;
            }
            case MODE_1_SIDE: {
                FrameLayout.LayoutParams leftViewLayoutParams = new LayoutParams(mLeftPanelWidth, getHeight());
                mLeftView.setLayoutParams(leftViewLayoutParams);
                
                FrameLayout.LayoutParams rightViewLayoutParams = new LayoutParams(mLeftPanelWidth, getHeight());
                rightViewLayoutParams.leftMargin = getWidth()-mLeftPanelWidth;
                rightViewLayoutParams.gravity = Gravity.TOP;
//                rightViewLayoutParams.addRule(ALIGN_PARENT_RIGHT);
                mRightView.setLayoutParams(rightViewLayoutParams);
                
                int topContainerWidth = getWidth() + mLeftPanelWidth;
                FrameLayout.LayoutParams topContainerLayoutParams = new LayoutParams(topContainerWidth, getHeight());
                topContainerLayoutParams.gravity = Gravity.TOP;
                topContainerLayoutParams.leftMargin = -mLeftPanelWidth;
                mTopContainer.setLayoutParams(topContainerLayoutParams);
                mTopContainer.setBackgroundColor(Color.TRANSPARENT);
                

                
                FrameLayout.LayoutParams topViewLayoutParams = new FrameLayout.LayoutParams(getWidth()-mLeftPanelWidth, getHeight());
                
                topViewLayoutParams.leftMargin = mLeftPanelWidth;
                topViewLayoutParams.gravity = Gravity.TOP;
                mTopView.setLayoutParams(topViewLayoutParams);
                
                
                if (mLeftShadow != null && mRightShadow != null) {
                    
                    FrameLayout.LayoutParams leftParams = new FrameLayout.LayoutParams(mLeftPanelWidth, LayoutParams.MATCH_PARENT);
                    FrameLayout.LayoutParams rightParams = new FrameLayout.LayoutParams(mLeftPanelWidth, LayoutParams.MATCH_PARENT);
                    rightParams.leftMargin = (getWidth());
                    rightParams.gravity = Gravity.TOP;
                    
                    mLeftShadow.setLayoutParams(leftParams);
                    mRightShadow.setLayoutParams(rightParams);
                    
                }
                
                int newFocus = DECK_UNKNOWN;
                switch(mCurrentDeckFocus) {
                    case DECK_UNKNOWN:
                    case DECK_TOP:
                    case DECK_LEFT:
                        mTopContainer.scrollTo(getMinScrollX(), 0);
                        mLeftView.setVisibility(View.VISIBLE);
                        mRightView.setVisibility(View.INVISIBLE);
                        newFocus = DECK_LEFT;
                        break;
                    case DECK_RIGHT:
                        mTopContainer.scrollTo(getMaxScrollX(), 0);
                        mLeftView.setVisibility(View.INVISIBLE);
                        mRightView.setVisibility(View.VISIBLE);
                        newFocus = DECK_RIGHT;
                        break;
                }
                onDeckFocusChanged(newFocus);
                if (mFocusChangedListener != null) {
                    mFocusChangedListener.onVisibilityChanged(mCurrentDeckFocus);
                }
                
                break;
            }
            case MODE_2_SIDES: {
                FrameLayout.LayoutParams leftViewLayoutParams = new LayoutParams(mLeftPanelWidth, getHeight());
                mLeftView.setLayoutParams(leftViewLayoutParams);
                
                FrameLayout.LayoutParams rightViewLayoutParams = new LayoutParams(mRightPanelWidth, getHeight());
                rightViewLayoutParams.leftMargin = getWidth() - mRightPanelWidth;
                rightViewLayoutParams.gravity = Gravity.TOP;
//                rightViewLayoutParams.addRule(ALIGN_PARENT_RIGHT);
                mRightView.setLayoutParams(rightViewLayoutParams);
                
                int topContainerWidth = getWidth();
                FrameLayout.LayoutParams topContainerLayoutParams = new LayoutParams(topContainerWidth, getHeight());
                topContainerLayoutParams.gravity = Gravity.TOP;
//                topContainerLayoutParams.leftMargin = -mLeftPanelWidth;
                mTopContainer.setLayoutParams(topContainerLayoutParams);
                mTopContainer.setBackgroundColor(Color.TRANSPARENT);
                

                
                FrameLayout.LayoutParams topViewLayoutParams = new FrameLayout.LayoutParams(getWidth()-mLeftPanelWidth-mRightPanelWidth, getHeight());
                
                topViewLayoutParams.leftMargin = mLeftPanelWidth;
                topViewLayoutParams.gravity = Gravity.TOP;
                mTopView.setLayoutParams(topViewLayoutParams);
                
                
                if (mLeftShadow != null && mRightShadow != null) {
                    
                    FrameLayout.LayoutParams leftParams = new FrameLayout.LayoutParams(mLeftPanelWidth, LayoutParams.MATCH_PARENT);
                    FrameLayout.LayoutParams rightParams = new FrameLayout.LayoutParams(mRightPanelWidth, LayoutParams.MATCH_PARENT);
                    rightParams.leftMargin = (getWidth() - mRightPanelWidth);
                    rightParams.gravity = Gravity.TOP;
                    
                    mLeftShadow.setLayoutParams(leftParams);
                    mRightShadow.setLayoutParams(rightParams);
                    
                }

                mLeftView.setVisibility(View.VISIBLE);
                mRightView.setVisibility(View.VISIBLE);

                mTopContainer.scrollTo(0, 0);
                if (mFocusChangedListener != null) {
                    mFocusChangedListener.onVisibilityChanged(DECK_LEFT);
                    mFocusChangedListener.onVisibilityChanged(DECK_RIGHT);
                }
                break;
            }
        }
        

        onDeckFocusChanged(mCurrentDeckFocus);
        
    }
    
    public int getDeckMode() {
        return mDeckMode;
    }
    
    public void showLeft(boolean animated) {
        if (mDeckMode == MODE_2_SIDES)
            return;
        if (animated)
            smoothScrollTo(getMinScrollX());
        else
            scrollTo(getMinScrollX(), true);
    }
    
    public void showRight(boolean animated) {
        if (mDeckMode == MODE_2_SIDES)
            return;
        if (animated)
            smoothScrollTo(getMaxScrollX());
        else
            scrollTo(getMaxScrollX(), true);
    }
    
    public void showTop(boolean animated) {
        if (mDeckMode == MODE_2_SIDES || mDeckMode == MODE_1_SIDE)
            return;
        if (animated)
            smoothScrollTo(getCenterScrollX());
        else
            scrollTo(getCenterScrollX(), true);
    }
    
    public boolean isTopFocused() {
        if (mDeckMode == MODE_2_SIDES || mDeckMode == MODE_1_SIDE)
            return true;
        int centerX = getCenterScrollX();
        int scrollX = mTopContainer.getScrollX();
        return scrollX == centerX;
    }

    public boolean isLeftFocused() {
        if (mDeckMode == MODE_2_SIDES)
            return true;
        int leftX = getMinScrollX();
        int scrollX = mTopContainer.getScrollX();
        return scrollX == leftX;
    }
    
    public boolean isRightFocused() {
        if (mDeckMode == MODE_2_SIDES)
            return true;
        int rightX = getMaxScrollX();
        int scrollX = mTopContainer.getScrollX();
        return scrollX == rightX;
    }
    
    private boolean isOkToScroll(MotionEvent ev) {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        
        
        if (mScrollingDisabled) {
//            CLog.v("scrolling disabled " + action);
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
        
        if (mDeckMode == MODE_0_SIDES) {
            if (scrollX == centerX)
                return true;
            else if (scrollX < centerX)
                return trueX > centerX;
            else
                return trueX < (centerX + getWidth());
        } else if (mDeckMode == MODE_1_SIDE) {
            if (scrollX == getMinScrollX())
                return x > mLeftPanelWidth;
            if (scrollX == getMaxScrollX())
                return x < (getWidth()-mRightPanelWidth);
        }
        return true;
    }
    
    
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        CLog.v("onInterceptTouchEvent");
        
        if (mDeckMode == MODE_2_SIDES)
            return false;
        
        if (!isOkToScroll(ev)) {
//            CLog.v("not ok to scroll");
            return false;
        }
        
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
                stopScrolling(true);
                return false;
            }

            if (dx > mTouchSlop) {
                mIsBeingDragged = true;
                mLastMotionX = x;
                setTopScrollingAllowed(false);
                mScrollXOnDown = mTopContainer.getScrollX();
            }
            break;
        }
        
        case MotionEvent.ACTION_DOWN: { 
            mScrollXOnDown = mTopContainer.getScrollX();
//            mTouchPointOnDown = (int) ev.getX();
            
            final float x = ev.getX();
            final float y = ev.getY();
            mLastMotionX = x;
            mLastMotionY = y;
            mIsBeingDragged = !mScroller.isFinished();
            if (mDeckMode == MODE_0_SIDES && mScrollXOnDown != getCenterScrollX()) {
                mTouchMightBeTap = true;
                return true;
            }
            break;
        }
        
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            mIsBeingDragged = false;
            setTopScrollingAllowed(true);
            mScrollXOnDown = null;
//            mTouchPointOnDown = null;
            break;
        }
        
        return mIsBeingDragged;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        CLog.v("onTouchEvent");
        
        if (mDeckMode == MODE_2_SIDES)
            return false;
        
        if (!isOkToScroll(event)) {
//            CLog.v("not ok to scroll");
            return false;
        }
        
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
                
//                int newScrollX = mTopContainer.getScrollX();
                
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    mIsBeingDragged = true;
                    mIsBeingScrolled = false;
                    setTopScrollingAllowed(false);
                }
                final float x = event.getX();
                final float y = event.getY();
                final int deltaX = (int) (mLastMotionX - x);
                final int deltaY = (int) (mLastMotionY - y);
                mLastMotionY = y;
                mLastMotionX = x;
                if (mTouchMightBeTap && (Math.abs(deltaX) > mTouchSlop || Math.abs(deltaY) > mTouchSlop)) {
                    mTouchMightBeTap = false;
                }
                scrollBy(deltaX, true);
                break;
            }
            
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (mTouchMightBeTap) {
                    mTouchMightBeTap = false;
                    if (DroidKit.DEBUG) CLog.v("TOUCH MIGHT BE TAP, SHGOWING TOP");
                    showTop(true);
                } else {
                
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    
                    int initialVelocity = (int) velocityTracker.getXVelocity();
                    
                    mIsBeingDragged = false;
                    if (Math.abs(initialVelocity) > mMinVelocity) {
                        fling(-initialVelocity);
                    } else {
                        finishScroll();
                    }
                }
                
                
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                
                setTopScrollingAllowed(true);
                mScrollXOnDown = null;
//                mTouchPointOnDown = null;
                
                break;
            }
        }
        return true;

    }
    
    private void finishScroll() {
        int scrollX = mTopContainer.getScrollX();
        final int maxX = getMaxScrollX();
        final int minX = getMinScrollX();
        final int centerX = getCenterScrollX();
        if (scrollX == centerX || scrollX == maxX || scrollX == minX) {
            mIsBeingDragged = false;
            mIsBeingScrolled = false;
            mScrollXOnDown = null;
            setTopScrollingAllowed(true);
            int newFocus = DECK_UNKNOWN;
            if (scrollX == centerX) {
                newFocus = DECK_TOP;
            } else if (scrollX == minX) {
                newFocus = DECK_LEFT;
            } else if (scrollX == maxX) {
                newFocus = DECK_RIGHT;
            }
            onDeckFocusChanged(newFocus);
        } else {
            if (mDeckMode == MODE_0_SIDES) {
                if (scrollX < (centerX + minX)/2)
                    smoothScrollTo(minX);
                else if (scrollX > (centerX + maxX)/2)
                    smoothScrollTo(maxX);
                else
                    smoothScrollTo(centerX);
            } else if (mDeckMode == MODE_1_SIDE) {
                int midX = (maxX + minX)/2;
                if (scrollX < midX) {
                    smoothScrollTo(minX);
                } else {
                    smoothScrollTo(maxX);
                }
            }
        }
    }
    
    protected void onDeckFocusChanged(int newFocus) {
        if (mCurrentDeckFocus != newFocus) {
            mCurrentDeckFocus = newFocus;
            if (mFocusChangedListener != null)
                mFocusChangedListener.onDeckFocusChanged(newFocus);
        }
    }
    
    
    @Deprecated
    private void setTopScrollingAllowed(boolean allowed) {
//        if (mTopScrollView != null) {
//            StoppableScrollView ss = mTopScrollView.get();
//            if (ss != null) {
//                if (allowed)
//                    ss.allowScrolling();
//                else 
//                    ss.stopScrolling();
//            }
//        }
    }
//    
//    public void setTopScrollView(StoppableScrollView parentScrollView) {
//        mTopScrollView = new WeakReference<StoppableScrollView>(parentScrollView);
//    }
    
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
        
        if (mDeckMode == MODE_0_SIDES) {
            if (scrollX < centerX) {
                if (mLeftView.getVisibility() != VISIBLE) {
                    if (mFocusChangedListener != null)
                        mFocusChangedListener.onVisibilityChanged(DECK_LEFT);
                    mLeftView.setVisibility(VISIBLE);
                    mRightView.setVisibility(INVISIBLE);
                }
            } else if (scrollX > centerX) {
                if (mRightView.getVisibility() != VISIBLE) {
                    if (mFocusChangedListener != null)
                        mFocusChangedListener.onVisibilityChanged(DECK_RIGHT);
                    mLeftView.setVisibility(INVISIBLE);
                    mRightView.setVisibility(VISIBLE);
                }
            } else if (scrollX == centerX) {
                mLeftView.setVisibility(INVISIBLE);
                mRightView.setVisibility(INVISIBLE);
            }
        } else if (mDeckMode == MODE_1_SIDE) {
            if (scrollX > minX) {
                if (mLeftView.getVisibility() != VISIBLE) {
                    if (mFocusChangedListener != null)
                        mFocusChangedListener.onVisibilityChanged(DECK_LEFT);
                    mLeftView.setVisibility(VISIBLE);
//                    mRightView.setVisibility(INVISIBLE);
                }
            }
            if (scrollX < maxX) {
                if (mRightView.getVisibility() != VISIBLE) {
                    if (mFocusChangedListener != null)
                        mFocusChangedListener.onVisibilityChanged(DECK_RIGHT);
//                    mLeftView.setVisibility(INVISIBLE);
                    mRightView.setVisibility(VISIBLE);
                }
            }
            if (scrollX == maxX)
                mLeftView.setVisibility(INVISIBLE);
            if (scrollX == minX) 
                mRightView.setVisibility(INVISIBLE);
        }
        
        mTopContainer.scrollTo(scrollX, 0);
        allowInvalidate();
    }
    
    protected void scrollBy(int dx, boolean invalidate) {
        scrollTo(mTopContainer.getScrollX()+dx, invalidate);
    }
    
//    protected void smoothScrollTo(int x) {
//        smoothScrollTo(x, 150);
//    }
    
    protected void smoothScrollTo(int x) { //, int duration) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        
        int scrollX = mTopContainer.getScrollX();
        int dx = x - scrollX;
        
        mScroller.startScroll(scrollX, 0, dx, 0, calculateScrollDuration(dx));
        invalidate();
    }
    
    protected int calculateScrollDuration(int dx) {
        float dxf = (float)Math.abs(dx);
        
        int maxWidth = getWidth()-mVisibleSideMarginPx;
//        float millisPerPixel = 0.35714287f;
        float millisPerPixel = 200f/(float)maxWidth;
//        CLog.v("millisPerPixel = " + millisPerPixel);
        float duration = millisPerPixel*dxf;
        return (int)duration;
    }
    
    protected void fling(int initVelocity) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        
        int scrollX = mTopContainer.getScrollX();
        int maxX = getMaxScrollX();
        int minX = getMinScrollX();
        int centerX = getCenterScrollX();
        
        if (mDeckMode == MODE_0_SIDES) {
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
        }
        
//        CLog.v("init fling velocity = " + initVelocity);
        int destX = initVelocity < 0 ? minX : maxX;
        if (DroidKit.DEBUG) CLog.v("FLING DEST X = " + destX);
        int dx = destX - scrollX;
        
//        mScroller.fling(scrollX, 0, initVelocity, 0, minX, maxX, 0, 0);
        mScroller.startScroll(scrollX, 0, dx, 0, calculateScrollDuration(dx));
        invalidate();
    }
    
    private void resetScrollBarriers() {
        if (getWidth() <= 0)
            return;
        mMaxScrollX = 0;
        switch(mDeckMode) {
            case MODE_0_SIDES:
                mMinScrollX = -((getWidth() * 2) - (mVisibleSideMarginPx * 2));
                mCenterScrollX = -(getWidth() - mVisibleSideMarginPx);
                break;
            case MODE_1_SIDE:
                mMinScrollX = -mLeftPanelWidth;
                mCenterScrollX = Integer.MIN_VALUE;
                break;
            case MODE_2_SIDES:
                mMinScrollX = 0;
                mCenterScrollX = 0;
                break;
        }
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
        stopScrolling(false);
    }
    
    public void stopScrolling(boolean resetTimer) {
        mScrollingDisabled = true;
        if (resetTimer)
            resetAllowScrollingTimer();
        else
            cancelAllowScrollingTimer();
    }

    @Override
    public void allowScrolling() {
        mScrollingDisabled = false;
        cancelAllowScrollingTimer();
    }

    @Override
    public boolean isScrollingAllowed() {
        return !mScrollingDisabled;
    }
    
    private Runnable mAllowScrollingRunnable = new Runnable() {
        
        @Override
        public void run() {
            allowScrolling();
        }
    };
    
    private void resetAllowScrollingTimer() {
        getHandler().removeCallbacks(mAllowScrollingRunnable);
        getHandler().postDelayed(mAllowScrollingRunnable, 100);
    }
    
    private void cancelAllowScrollingTimer() {
        getHandler().removeCallbacks(mAllowScrollingRunnable);
    }
    
}

